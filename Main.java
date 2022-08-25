import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.ValueLayout.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;

public class Main {
    private static class GoString {
        private static final OfAddress POINTER = ADDRESS.withName("p");
        private static final OfLong NUMBER = JAVA_LONG.withName("n");

        private static final GroupLayout LAYOUT = MemoryLayout.structLayout(POINTER, NUMBER);

        private static final VarHandle P = LAYOUT.varHandle(PathElement.groupElement(POINTER.name().get()));
        private static final VarHandle N = LAYOUT.varHandle(PathElement.groupElement(NUMBER.name().get()));

        public static MemorySegment allocate(String str, ResourceScope scope) {
            MemorySegment memory = MemorySegment.allocateNative(LAYOUT, scope);

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
        CLinker linker = CLinker.systemCLinker();
        SymbolLookup lookup = SymbolLookup.loaderLookup();
        MethodHandle recv = linker.downcallHandle(
            lookup.lookup("recv").get(),
                FunctionDescriptor.ofVoid(GoString.LAYOUT));

        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            recv.invoke(GoString.allocate(message, scope));
        }
    }

    private static void recv() throws Throwable {
        CLinker linker = CLinker.systemCLinker();
        SymbolLookup lookup = SymbolLookup.loaderLookup();
        MethodHandle recv = linker.downcallHandle(
                lookup.lookup("send").get(),
                FunctionDescriptor.of(GoString.LAYOUT));

        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            MemorySegment address = (MemorySegment) recv.invoke(scope);
            System.out.println(GoString.get(address));
        }
    }
}
