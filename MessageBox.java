import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;

import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

public class MessageBox {
    private static final int MB_ICONINFORMATION = 0x00000040;
    private static final int MB_OKCANCEL = 0x00000001;

    private static class LPCTSTR {
        private static final MemoryLayout LAYOUT = ADDRESS.withName("LPCTSTR");
        
        public static MemoryAddress allocate(String str, MemorySession session) {
            byte[] b = str.getBytes(StandardCharsets.UTF_16LE);
            MemorySegment ptr = SegmentAllocator.implicitAllocator().allocate(b.length + 1);
            for (int i = 0; i < b.length; i++) {
                ptr.set(JAVA_BYTE, i, b[i]);
            }
            ptr.set(JAVA_BYTE, b.length, (byte) 0);
            
            return ptr.address();
        }
    }

    static {
        System.loadLibrary("User32");
    }

    public static void main(String[] args) throws Throwable {
        Linker linker = Linker.nativeLinker();
        SymbolLookup lookup = SymbolLookup.loaderLookup();
        MethodHandle recv = linker.downcallHandle(
                lookup.lookup("MessageBoxW").get(),
                FunctionDescriptor.of(JAVA_INT, JAVA_LONG, LPCTSTR.LAYOUT, LPCTSTR.LAYOUT, JAVA_INT));

        try (MemorySession session = MemorySession.openConfined()) {
            MemoryAddress text = LPCTSTR.allocate("Hello world!", session);
            MemoryAddress caption = LPCTSTR.allocate("Java", session);

            int ret = (int) recv.invoke(0, text, caption, MB_OKCANCEL | MB_ICONINFORMATION);

            System.out.println(ret);
        }
    }
}
