package com.apploidxxx.heliosrestapispring.api.util.chain;


import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Arthur Kupriyanov
 */
@Slf4j
public class CommandChain {

    /**
     * Return a chains of command {@link Chain} which have
     * {@link Chain#getAction(String)}.
     * <br/><br/>
     * You need to provide an instance of Object which have inner {@link Command} classes
     * It's necessary because inner-nonstatic-classes needs an instance of outer class
     *
     *
     * @param controller instance of outer class
     * @param implementationFunctionalInterface interface is implemented by {@link Command} classes
     *                                          annotation {@link FunctionalInterface} there is no needed
     *                                          but recommended use kind of
     * @param <T> class-type mark for interface
     * @return Chain of {@link Command}s
     */
    public <T> Chain<T> init(Object controller, Class<T> implementationFunctionalInterface) {

        Map<String, T> actionsMap = new HashMap<>();
        for (Class<?> declaredClass : controller.getClass().getDeclaredClasses()) {

            Command annotation;

            if ((annotation = declaredClass.getAnnotation(Command.class)) == null) continue;

            log.debug("Founded action annotated with Command annotation: " + declaredClass.getName());

            boolean implementedAction = false;
            for (Class i : declaredClass.getInterfaces()) {
                if (i == implementationFunctionalInterface) {
                    implementedAction = true;
                    break;
                }
            }
            if (!implementedAction) {
                log.error("Class " + declaredClass.getName() + " not implemented " + implementationFunctionalInterface.getName());
                continue;
            }

            if (declaredClass.isInstance(implementationFunctionalInterface)) continue;
            log.debug("Successfully implemented declared interface : " + implementationFunctionalInterface.getName());

            try {
                for (Constructor<?> c : declaredClass.getDeclaredConstructors()) {
                    Constructor<T> castedConstructor = (Constructor<T>) c;

                    T commandInstance = null;
                    castedConstructor.setAccessible(true);

                    if (castedConstructor.getParameterTypes().length == 1) {

                        if (!castedConstructor.getParameterTypes()[0].isInstance(controller)) continue;

                        commandInstance = castedConstructor.newInstance(controller);
                        log.debug("Put Action with name " + c.getName() + " to actions map");

                    } else if (castedConstructor.getParameterTypes().length == 0){
                        if (Modifier.isStatic(castedConstructor.getDeclaringClass().getModifiers())){
                            castedConstructor.setAccessible(true);
                            commandInstance = castedConstructor.newInstance();
                            log.debug("Put Action with name " + c.getName() + " to actions map");

                        }
                    }
                    if (commandInstance == null) continue;
                    T oldValue = actionsMap.put(annotation.value(), commandInstance);
                    if (oldValue != null){
                        log.warn("Action " + oldValue.getClass().getName() + " replaced by " + commandInstance.getClass().getName() + " on key " + annotation.value());
                    }
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("Can't initialize class", e);
            }
        }
        log.debug("Actions map: " + actionsMap.toString());
        return new Chain<>(actionsMap);

    }

}
