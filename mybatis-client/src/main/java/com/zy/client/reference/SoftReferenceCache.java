package com.zy.client.reference;

/*******************************************************
 * Created by ZhangYu on 2024/11/2
 * Description :ReferenceQueue使用
 * History   :
 *******************************************************/

import lombok.Data;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Data
class CachedObject {
    private final String data;

    public CachedObject(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }



    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("CachedObject被垃圾回收: " + data);
    }
}

public class SoftReferenceCache {

    // 缓存数据的Map，使用SoftReference
    private final Map<String, SoftReference<CachedObject>> cache = new HashMap<>();

    // ReferenceQueue，用于跟踪被回收的缓存对象
    private final ReferenceQueue<CachedObject> referenceQueue = new ReferenceQueue<>();

    // 添加对象到缓存
    public void put(String key, CachedObject value) {
        // 将对象包装成SoftReference并与ReferenceQueue关联
        SoftReference<CachedObject> softRef = new SoftReference<>(value, referenceQueue);
        cache.put(key, softRef);
    }

    // 从缓存中获取对象
    public CachedObject get(String key) {
        SoftReference<CachedObject> softRef = cache.get(key);
        if (softRef != null) {
            return softRef.get(); // 获取SoftReference中的对象
        }
        return null;
    }



    public static void main(String[] args) {
        SoftReferenceCache softReferenceCache = new SoftReferenceCache();

        // 添加对象到缓存
        softReferenceCache.put("item1", new CachedObject("数据1"));
        softReferenceCache.put("item2", new CachedObject("数据2"));

        // 尝试从缓存中获取对象
        System.out.println("从缓存中获取 item1: " + softReferenceCache.get("item1").getData());

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 模拟内存不足
                for (int i = 0; i < 100000000; i++) {
                    softReferenceCache.put(String.valueOf(i), new CachedObject("数据"+i));
                }
            }
        }).start();
        // 暂停一段时间，以便触发垃圾回收
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 请求垃圾回收
        System.gc();
        SoftReference<? extends CachedObject> ref;
        while ((ref = (SoftReference<? extends CachedObject>) softReferenceCache.referenceQueue.poll()) != null) {
            // 这里可以执行清理逻辑
            System.out.println("从缓存中移除被回收的对象");
            // 找到并移除这个引用对应的键
            softReferenceCache.cache.values().remove(ref);
        }
    }
}
