import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.ValueLayout.*;

import java.lang.invoke.MethodHandle;

public class Main {
    private static GroupLayout GO_STRING = MemoryLayout.structLayout(
            ADDRESS.withName("p"),
            JAVA_LONG.withName("n"));

    public static void main(String[] args) throws Throwable {
        System.loadLibrary("gopher");

        send("Hello Gopher!");
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
            offHeap.set(ValueLayout.ADDRESS, 0, cString);
            offHeap.set(ValueLayout.JAVA_LONG, 8, cString.byteSize());

            recv.invoke(offHeap);
        }
    }
}
