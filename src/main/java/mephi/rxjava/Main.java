package mephi.rxjava;

public class Main {
    public static void main(String[] args) {
        IOThreadScheduler ioScheduler = new IOThreadScheduler();
        ComputationScheduler computationScheduler = new ComputationScheduler();

        Observable<Integer> observable = Observable.create(emitter -> {
            for (int i = 1; i <= 5; i++) {
                emitter.onNext(i);
            }
            emitter.onComplete();
        });

        observable
                .map(i -> i * 10)
                .filter(i -> i % 20 == 0)
                .subscribeOn(ioScheduler)
                .observeOn(computationScheduler)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        System.out.println("Received: " + item + " on " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.err.println("Error: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("Completed on: " + Thread.currentThread().getName());
                    }
                });

        // Ждём завершения асинхронных потоков
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Завершаем Executor'ы
        ioScheduler.shutdown();
        computationScheduler.shutdown();
    }
}
