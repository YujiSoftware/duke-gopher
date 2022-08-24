import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;

public class Main19 {
    private static class GoString {
        private static final OfAddress POINTER = ADDRESS.withName("p");
        private static final OfLong NUMBER = JAVA_LONG.withName("n");

        private static final GroupLayout LAYOUT = MemoryLayout.structLayout(POINTER, NUMBER);

        private static final VarHandle P = LAYOUT.varHandle(PathElement.groupElement(POINTER.name().get()));
        private static final VarHandle N = LAYOUT.varHandle(PathElement.groupElement(NUMBER.name().get()));

        public static MemorySegment allocate(String str, MemorySession session) {
            MemorySegment memory = MemorySegment.allocateNative(LAYOUT, session);

            MemorySegment cString = SegmentAllocator.implicitAllocator().allocateUtf8String(str);
            P.set(memory, cString.address());
            N.set(memory, cString.byteSize());

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
        send("Hello Gopher! (from Duke)");
        recv();
    }

    private static void send(String message) throws Throwable {
        Linker linker = Linker.nativeLinker();
        SymbolLookup loaderLookup = SymbolLookup.loaderLookup();
        MethodHandle recv = linker.downcallHandle(
                loaderLookup.lookup("recv").get(),
                FunctionDescriptor.ofVoid(GoString.LAYOUT));

        try (MemorySession session = MemorySession.openConfined()) {
            recv.invoke(GoString.allocate(message, session));
        }
    }

    private static void recv() throws Throwable {
        Linker linker = Linker.nativeLinker();
        SymbolLookup loaderLookup = SymbolLookup.loaderLookup();
        MethodHandle recv = linker.downcallHandle(
                loaderLookup.lookup("send").get(),
                FunctionDescriptor.of(GoString.LAYOUT));

        try (MemorySession session = MemorySession.openConfined()) {
            MemorySegment address = (MemorySegment) recv.invoke(session);
            System.out.println(GoString.get(address));
        }
    }
}
