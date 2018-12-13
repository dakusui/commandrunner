package com.github.dakusui.cmd.pipeline;

import com.github.dakusui.cmd.core.stream.Merger;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dakusui.cmd.pipeline.Cmd.Type.PIPE;
import static com.github.dakusui.cmd.pipeline.Cmd.Type.SOURCE;
import static com.github.dakusui.cmd.utils.Checks.greaterThan;
import static com.github.dakusui.cmd.utils.Checks.isNull;
import static com.github.dakusui.cmd.utils.Checks.requireArgument;
import static com.github.dakusui.cmd.utils.Checks.requireState;
import static com.github.dakusui.cmd.utils.Checks.typeIs;
import static java.util.Objects.requireNonNull;

public interface Cmd {
  enum Type {
    SOURCE,
    PIPE,
    SINK,
    COMMAND
  }

  Type type();

  Cmd connect(Cmd... cmds);

  Stream<String> stream();

  Cmd map(Cmd cmd);

  Cmd reduce(Cmd cmd);

  interface Factory {
    default Cmd source(String command) {
      return new Impl(SOURCE, numSplits());
    }

    default Cmd pipe(String command) {
      return new Impl(PIPE, numSplits());
    }

    default Cmd sink(String command) {
      return new Impl(Type.SINK, numSplits());
    }

    default Cmd cmd(String command) {
      return new Impl(Type.COMMAND, numSplits());
    }

    default int numSplits() {
      return 8;
    }
  }

  class Impl implements Cmd {
    private final Type type;
    boolean alreadyStreamed = false;
    Cmd     upstream        = null;
    Cmd[]   downstreams     = null;


    Impl(Type type, int numSplits) {
      this.type = requireNonNull(type);
    }

    @Override
    public Type type() {
      return this.type;
    }

    @Override
    public Cmd connect(Cmd... cmds) {
      requireState(this, typeIs(SOURCE).or(typeIs(PIPE)));
      requireState(this.downstreams, isNull().negate());
      requireArgument(cmds.length, greaterThan(0));
      return this;
    }

    @Override
    public Cmd map(Cmd cmd) {
      return null;
    }

    @Override
    public Cmd reduce(Cmd cmd) {
      return null;
    }

    @Override
    public Stream<String> stream() {
      requireState(this, isAlreadyStreamed());
      alreadyStreamed = true;
      return new Merger.Builder<>(
          Arrays.stream(downstreams)
              .map(Cmd::stream)
              .collect(Collectors.toList()))
          .build()
          .merge();
    }

    private Predicate<Impl> isAlreadyStreamed() {
      return new Predicate<Impl>() {
        @Override
        public boolean test(Impl impl) {
          return impl.alreadyStreamed;
        }

        @Override
        public String toString() {
          return "isAlreadyStreamed";
        }
      };
    }
  }
}