package com.apploidxxx.heliosrestapispring.api.util.chain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Arthur Kupriyanov
 */
public class CommandChainTest {
    private Chain<Executable> chain = new CommandChain().init(new TestClass(), Executable.class);

    @Test
    public void executeMethods() {
        String property = "value";
        String actual1 = chain.getAction("m1").exec(property);
        String actual2 = chain.getAction("m2").exec(property);

        assertEquals(property + TestClass.val1, actual1);
        assertEquals(property + TestClass.val2, actual2);
    }

    @Test
    public void addActions(){
        String property = "value";

        chain.addAction("m3", (val) -> "Some" + val);
        String actual = chain.getAction("m3").exec(property);

        assertEquals("Some" + property, actual);

        chain.addAction(new CommandThree(), Executable.class);
        assertEquals(property + "m4", chain.getAction("m4").exec(property));
    }

    @Test
    public void actionNotFoundException() {
        try {
            chain.getAction("@g23g23fasdf").exec("some property");
        } catch (Exception e){
            assertTrue(e instanceof ActionNotFoundException);
        }
    }
    @Test
    public void incorrectCommandsUse() {
        chain.addAction(new BadCommandOuterClass(), Executable.class);
        chain.getAction("incorrect_command_duplicate").exec("some value");
    }
}

class CommandThree {


    @Command("m4")
    static class AdditionalCommand implements Executable{

        @Override
        public String exec(String val) {
            return val + "m4";
        }
    }
}

class BadCommandOuterClass {
    @Command("incorrect_command")
    static class IncorrectCommandWithoutImpl {

    }

    @Command("incorrect_command_without_def_constr")
    static class IncorrectCommandWithoutNoArgConstructorOnStatic {
        IncorrectCommandWithoutNoArgConstructorOnStatic(String s1, String s2){

        }
    }

    @Command("incorrect-non-static-without-correct-consr")
    class IncorrectCommandWithoutConstOnNonStatic{
        IncorrectCommandWithoutConstOnNonStatic(){

        }
        IncorrectCommandWithoutConstOnNonStatic(String s1, int b){

        }
    }

    @Command("incorrect_command_duplicate")
    static class Incorrect1 implements Executable{

        @Override
        public String exec(String val) {
            return val;
        }
    }

    @Command("incorrect_command_duplicate")
    static class Incorrect2 implements Executable{

        @Override
        public String exec(String val) {
            return val;
        }
    }
}

class TestClass {

    static final String val1 = "@";
    static final String val2 = "$";

    @Command("m1")
    private class CommandOne implements Executable{

        @Override
        public String exec(String val) {
            return val + val1;
        }
    }

    @Command("m2")
    private static class CommandTwo implements Executable{
        CommandTwo(String s1){

        }

        CommandTwo(){

        }

        @Override
        public String exec(String val) {
            return val + val2;
        }
    }
}


@FunctionalInterface
interface Executable{
    String exec(String val);
}