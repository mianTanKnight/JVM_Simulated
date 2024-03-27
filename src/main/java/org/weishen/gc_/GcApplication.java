package org.weishen.gc_;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GcApplication {

    public static void main(String[] args) {
        //    private void performGC(String generation) throws Exception {
//        //get writerLock
//        ReentrantReadWriteLock.WriteLock writeLock = stwLockOfApp.writeLock();
//        try {
//            writeLock.lock();
//            // 根据代来触发不同类型的GC
//            if (OLD_.equals(generation)) {
//                performFullGC();
//            } else {
//                performMinorGC();
//            }
//        } finally {
//            writeLock.unlock();
//        }
//    }


//    private void performMinorGC(){
//        //todo
//    }
//
//    private void performFullGC(){
//        //todo
//    }


        SpringApplication.run(GcApplication.class, args);
    }

}
