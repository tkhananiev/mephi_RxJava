package mephi.rxjava;

public class Main {
    public static void main(String[] args) {
        Observable<Integer> observable = Observable.create(emitter -> {
            for (int i = 1; i <= 5; i++) {
                emitter.onNext(i);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    emitter.onError(e);
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            emitter.onComplete();
        });

        observable
                .map(i -> i * 10)
                .filter(i -> i % 20 == 0)
                .subscribeOn(new IOThreadScheduler())
                .observeOn(new ComputationScheduler())
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

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
