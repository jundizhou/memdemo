
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class ReadMemory {
    String fileName = "shm.lock";
    RandomAccessFile raFile;
    FileChannel fc;
    int iSize = 1024;
    MappedByteBuffer mapBuf;
    int iMode;
    int lastIndex = 0;

    public ReadMemory() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() throws Exception {
        raFile = new RandomAccessFile(fileName, "rw");
        fc = raFile.getChannel();
        mapBuf = fc.map(FileChannel.MapMode.READ_WRITE, 0, iSize);
    }

    public void clearBuffer() {
        // 清除文件内容
        for (int i = 0; i < 1024; i++) {
            mapBuf.put(i, (byte) 0);
        }
    }

    public void getBuffer() throws Exception{
        for(int i=1;i<27;i++){
            int flag = mapBuf.get(0); //取读写数据的标志
            int index = mapBuf.get(1); //读取数据的位置,2 为可读

            if(flag != 2 || index == lastIndex){ //假如不可读，或未写入新数据时重复循环
                i--;
                continue;
            }

            lastIndex = index;
            System.out.println("程序 ReadShareMemory：" + System.currentTimeMillis() +
                    "：位置：" + index +" 读出数据：" + (char)mapBuf.get(index));

            mapBuf.put(0,(byte)0); //置第一个字节为可读标志为 0

            if(index == 27){ //读完数据后退出
                break;
            }
        }
    }

    public boolean getLock() {
        FileLock lock = null;
        try {
            lock = fc.tryLock();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (lock == null) {
            return false;
        } else {
            return true;
        }
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        ReadMemory map = new ReadMemory();
        if (map.getLock()) {
            try {
                map.getBuffer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("can't get lock");
        }
    }
}
