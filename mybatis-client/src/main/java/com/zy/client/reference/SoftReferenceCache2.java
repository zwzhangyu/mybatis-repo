package com.zy.client.reference;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/*******************************************************
 * Created by ZhangYu on 2024/11/2
 * Description :
 * History   :
 *******************************************************/
public class SoftReferenceCache2 {

    public static void main(String[] args) {
        simpleRef();
        refWithReferenceQueue();
    }

    private static void simpleRef() {
        // 通过等号直接建立的引用都是强引用
        User user = new User();

        // 通过SoftReference建立的引用是软引用
        SoftReference<User> softRefUser =new SoftReference<>(new User());

        // 通过WeakReference建立的引用是弱引用
        WeakReference<User> weakRefUser = new WeakReference<>(new User());
    }

    private static void refWithReferenceQueue() {
        // 创建ReferenceQueue
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
        // 用来存储弱引用的目标对象
        List<WeakReference> weakRefUserList = new ArrayList<>();
        // 创建大量的弱引用对象，交给weakRefUserList引用
        for (int i =0 ; i< 1000000; i++) { // 模拟内存不足
            // 创建弱引用对象，并在此过程中传入ReferenceQueue
            WeakReference<User> weakReference = new WeakReference(new User(Math.round(Math.random() * 1000)),referenceQueue);
            // 引用弱引用对象
            weakRefUserList.add(weakReference);
        }
        WeakReference weakReference;
        Integer count = 0;
        // 处理被回收的弱引用
        while ((weakReference = (WeakReference) referenceQueue.poll()) != null) {
            // 虽然弱引用存在，但是引用的目标对象已经为空
            System.out.println("JVM 清理了：" + weakReference + ", 从WeakReference中取出对象值为：" + weakReference.get());
            count ++;
        }
        // 被回收的弱引用总数
        System.out.println("weakReference中的元素数目为：" + count);
        // 在弱引用的目标对象不被清理时，可以引用到目标对象
        System.out.println("在不被清理的情况下，可以从WeakReference中取出对象值为：" +
                new WeakReference(new User(Math.round(Math.random() * 1000)),referenceQueue).get());
    }

   static   class User {
        private long id;

        public User() {
        }

        public User(long id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "User:" + id;
        }
    }
}
