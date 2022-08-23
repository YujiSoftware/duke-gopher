import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.ValueLayout.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

public class Main {
    private static final OfAddress POINTER = ADDRESS.withName("p");
    private static final OfLong NUMBER = JAVA_LONG.withName("n");

    private static final GroupLayout GO_STRING = MemoryLayout.structLayout(POINTER, NUMBER);

    private static final VarHandle GO_STRING_P = GO_STRING.varHandle(PathElement.groupElement(POINTER.name().get()));
    private static final VarHandle GO_STRING_N = GO_STRING.varHandle(PathElement.groupElement(NUMBER.name().get()));

    public static void main(String[] args) throws Throwable {
        System.loadLibrary("gopher");

        send("Hello Gopher! (from Duke)");
    }

    private static void send(String message) throws Throwable {
        CLinker linker = CLinker.systemCLinker();
        SymbolLookup loaderLookup = SymbolLookup.loaderLookup();
        MethodHandle recv = linker.downcallHandle(
                loaderLookup.lookup("recv").get(),
                FunctionDescriptor.ofVoid(ADDRESS));

        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            MemorySegment offHeap = MemorySegment.allocateNative(GO_STRING, scope);
            MemorySegment cString = SegmentAllocator.implicitAllocator().allocateUtf8String(message);
            GO_STRING_P.set(offHeap, cString.address());
            GO_STRING_N.set(offHeap, cString.byteSize());

            recv.invoke(offHeap);
        }
    }
}
