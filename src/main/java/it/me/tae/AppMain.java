package it.me.tae;

import rx.Observable;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;
import rx.subjects.AsyncSubject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AppMain {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);


        // Background upload
        Map<String, AsyncSubject<String>> uploadMap = new HashMap<>();
        uploadMap.put("test1.png", uploadImageSubject("test1.png", 10));
        uploadMap.put("test2.png", uploadImageSubject("test2.png", -1));
        uploadMap.put("test3.png", uploadImageSubject("test3.png", 1));
        TimeUnit.SECONDS.sleep(2);







        // Check upload has error
        System.out.println("########");
        for (Map.Entry<String, AsyncSubject<String>> entry : uploadMap.entrySet()) {
            if (entry.getValue().hasThrowable()) {
                uploadMap.put(entry.getKey(), uploadImageSubject(entry.getKey(), 10));
            }
        }


        // Upload with call save
        Observable.zip(uploadMap.values(), new FuncN<List<String>>() {
            @Override
            public List<String> call(Object... args) {
                List<String> filenames = new ArrayList<>();
                for (Object filename : args) {
                    filenames.add((String) filename);
                }
                return filenames;
            }
        })
                .doOnNext(System.out::println)
                .doOnNext(__ -> System.out.println("Call save()"))
                .subscribe(
                        __ -> System.out.println("\tsuccess"),
                        Throwable::printStackTrace,
                        () -> latch.countDown()
                );


        latch.await();
    }

    public static AsyncSubject<String> uploadImageSubject(String image, int delay) {
        AsyncSubject<String> subject = AsyncSubject.create();
        Observable.just(image)
                .doOnNext(i -> System.out.println("uploading " + i))
                .map(i -> {
                    if (delay < 1) {
                        System.out.println("\tGenerate Error");
                        throw new RuntimeException("Generate Error");
                    } else {
                        return i.toUpperCase();
                    }
                })
                .delay(delay, TimeUnit.SECONDS)
                .doOnNext(i -> System.out.println("uploaded " + i))
                .subscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread()).subscribe(subject);
        return subject;
    }
}
