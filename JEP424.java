import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;

public class JEP424 {
    private static class GoString {
        private static final OfAddress POINTER = ADDRESS.withName("p");
        private static final OfLong NUMBER = JAVA_LONG.withName("n");

        private static final GroupLayout LAYOUT = MemoryLayout.structLayout(POINTER, NUMBER);

        private static final VarHandle P = LAYOUT.varHandle(PathElement.groupElement(POINTER.name().get()));
        private static final VarHandle N = LAYOUT.varHandle(PathElement.groupElement(NUMBER.name().get()));

        public static MemorySegment allocate(String str, MemorySession session) {
            byte[] b = str.getBytes(StandardCharsets.UTF_8);
            MemorySegment ptr = SegmentAllocator.implicitAllocator().allocate(b.length);
            for (int i = 0; i < b.length; i++) {
                ptr.set(JAVA_BYTE, i, b[i]);
            }

            MemorySegment memory = MemorySegment.allocateNative(LAYOUT, session);
            P.set(memory, ptr.address());
            N.set(memory, ptr.byteSize());

            return memory;
        }

        public static String get(MemorySegment memory) {
            int capacity = (int) (long) N.get(memory);
            MemoryAddress ptr = (MemoryAddress) P.get(memory);

            byte[] b = new byte[capacity];
            for (int i = 0; i < capacity; i++) {
                b[i] = ptr.get(JAVA_BYTE, i);
            }
            return new String(b, StandardCharsets.UTF_8);
        }
    }

    static {
        System.loadLibrary("gopher");
    }

    public static void main(String[] args) throws Throwable {
        talk("Hello Gopher! (from Duke)");
    }

    private static void talk(String message) throws Throwable {
        Linker linker = Linker.nativeLinker();
        SymbolLookup lookup = SymbolLookup.loaderLookup();
        MethodHandle recv = linker.downcallHandle(
                lookup.lookup("talk").get(),
                FunctionDescriptor.of(GoString.LAYOUT, GoString.LAYOUT));

        try (MemorySession session = MemorySession.openConfined()) {
            MemorySegment address = (MemorySegment) recv.invoke(session, GoString.allocate(message, session));

            System.out.println(GoString.get(address));
        }
    }
}
