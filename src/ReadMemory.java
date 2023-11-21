
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;

public class ReadMemory {
    String fileName = "shm.lock";
    RandomAccessFile raFile;
    static FileChannel fc;
    MappedByteBuffer mapBuf;
    int fileSize = 0;


    public void initMemFile(String fileName, int size){
        try {
            raFile = new RandomAccessFile(fileName, "rw");
            fc = raFile.getChannel();
            mapBuf = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearBuffer() {
        // 清除文件内容
        for (int i = 0; i < fileSize; i++) {
            mapBuf.put(i, (byte) 0);
        }
        mapBuf.position(0);
    }



    public String readMemFile() throws IOException {

        FileLock lock = null;
        try {
            lock = fc.tryLock();
        } catch (IOException e) {
            lock.release();
            e.printStackTrace();
        }
        if (lock != null) {
            try {
                getBuffer();
                lock.release();
            } catch (Exception e) {
                lock.release();
                e.printStackTrace();
            }
        } else {
            System.out.println("can't get lock");
        }
        return "ok";
    }

    public void getBuffer() throws Exception{
        //mapBuf.put(0,(byte)2);
        while (mapBuf.hasRemaining()) {
            int length = mapBuf.getInt(); // 读取数据长度元数据
            if(length == 0){
                break;
            }
            byte[] readData = new byte[length];
            System.out.println(length);
            mapBuf.get(readData); // 读取实际数据
            System.out.println(new String(readData)); // 输出数据
        }
        //mapBuf.put(0,(byte)0);
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

    public static void main(String[] args) throws IOException, InterruptedException {
        // TODO Auto-generated method stub
        ReadMemory readMemory = new ReadMemory();
        readMemory.initMemFile("shm.lock",  8 * 1024 * 1024);
        while (true) {
            readMemory.readMemFile();
            readMemory.clearBuffer();
            Thread.sleep(5000);
        }

    }
}
