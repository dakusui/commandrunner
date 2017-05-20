package com.github.dakusui.cmd.ut;

import com.github.dakusui.cmd.utils.TestUtils;
import com.github.dakusui.cmd.core.Selector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelectorTest extends TestUtils.TestBase {
  @Test(timeout = 5_000)
  public void main() {
    ExecutorService executorService = Executors.newFixedThreadPool(3);
    try {
      Selector<String> selector = new Selector.Builder<String>()
          .add(list("A", 10).stream())
          .add(list("B", 20).stream())
          .add(list("C", 30).stream().map(s -> {
            try {
              Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
            return s;
          }), s -> System.err.println("not redirected:" + s))
          .setQueueSize(3)
          .withExecutorService(executorService)
          .build();
      try {
        selector.select().forEach(s -> System.err.println("taken:" + s));
      } finally {
        selector.close();
      }
    } finally {
      System.out.println("shutting down");
      executorService.shutdown();
    }
  }

  private static List<String> list(String prefix, int size) {
    List<String> ret = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      ret.add(String.format("%s-%s", prefix, i));
    }
    return ret;
  }
}
