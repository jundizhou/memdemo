import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Random;
import java.nio.charset.StandardCharsets;

public class WriteMemory {
    RandomAccessFile raFile;
    FileChannel fc;
    MappedByteBuffer mapBuf;
    int fileSize = 0;
    int currentIndex = 1;

    public boolean initMemFile(String fileName, int size) {
        try {
            raFile = new RandomAccessFile(fileName, "rw");
            fc = raFile.getChannel();
            mapBuf = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < size; i++) {
            mapBuf.put(i, (byte) 0);
        }
        fileSize = size;
        return true;
    }

    public String writeMemFile(int length, byte[] content) throws IOException {
        FileLock lock = null;
        try {
            lock = fc.tryLock();
        } catch (IOException e) {
            lock.release();
            e.printStackTrace();
        }
        if (lock != null) {
            try {
                putBuffer(length, content);
                lock.release();
            } catch (Exception e) {
                lock.release();
                e.printStackTrace();
            }
        } else {
            // TODO: 重试
            System.out.println("can't get lock");
        }
        return "ok";
    }


    // index = 0为标志位，0为空闲，1为正在写，2为正在读。预留,如果后续tryFileLock性能不行可以用此标志位来控制并发读写
    public void putBuffer(int length, byte[] content){
        if(currentIndex+length>=fileSize) {
            System.out.println("drop event!");
            return;
        }
        //mapBuf.put(0, (byte) 1);
        mapBuf.putInt(length);
        mapBuf.put(content);
        currentIndex = currentIndex + length;
        //mapBuf.put(0, (byte) 0);
    }






    // 以下为测试代码


    public static void main(String[] args) throws IOException, InterruptedException {
        // TODO Auto-generated method stub
        WriteMemory writeMemory = new WriteMemory();
        writeMemory.initMemFile("shm.lock",  8 * 1024 * 1024);
        while (true) {
            String randomString = generateRandomString();
            byte[] bytes = randomString.getBytes(StandardCharsets.UTF_8);
            System.out.println("start write: " + new String(bytes));
            System.out.println("length: " + bytes.length);
            writeMemory.writeMemFile(bytes.length, bytes);
            Thread.sleep(1000);
        }

    }
    private static String generateRandomString() {
        Random random = new Random();
        int length = random.nextInt(500) + 1; // 生成 1 到 500 之间的随机长度
        StringBuilder sb = new StringBuilder();

        // 生成随机字符串
        for (int i = 0; i < length; i++) {
            char randomChar = (char) (random.nextInt(26) + 'a'); // 生成随机字符，这里以小写字母为例
            sb.append(randomChar);
        }

        return sb.toString();
    }

}
