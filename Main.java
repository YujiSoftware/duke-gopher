import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.ValueLayout.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

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

        public static String get(MemorySegment ptr) {
            return ptr.getUtf8String(0);
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
        SymbolLookup loaderLookup = SymbolLookup.loaderLookup();
        MethodHandle recv = linker.downcallHandle(
                loaderLookup.lookup("recv").get(),
                FunctionDescriptor.ofVoid(GoString.LAYOUT));

        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            recv.invoke(GoString.allocate(message, scope));
        }
    }

    private static void recv() throws Throwable {
        CLinker linker = CLinker.systemCLinker();
        SymbolLookup loaderLookup = SymbolLookup.loaderLookup();
        MethodHandle recv = linker.downcallHandle(
                loaderLookup.lookup("send").get(),
                FunctionDescriptor.of(GoString.LAYOUT));

        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            MemorySegment address = (MemorySegment) recv.invoke(scope);
            System.out.println(GoString.get(address));
        }
    }
}
