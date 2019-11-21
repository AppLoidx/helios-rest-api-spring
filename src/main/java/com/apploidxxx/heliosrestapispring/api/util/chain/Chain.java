package com.apploidxxx.heliosrestapispring.api.util.chain;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author Arthur Kupriyanov
 */
@Slf4j
public class Chain<T> {

    /**
     * Map of command instances and their keys
     * @param actionsMap key is the name of command which will be called from {@link #getAction(String)} method
     */
    Chain(Map<String, T> actionsMap) {
        this.actionsMap = actionsMap;
    }

    private Map<String, T> actionsMap;

    /**
     * Get {@link T} instance with key property
     * @param property key of command
     * @return instance of Command
     */
    public T getAction(String property) {
        T action = actionsMap.get(property);
        if (action == null) {
            throw new ActionNotFoundException();
        }
        return action;
    }

    /**
     * Add new action with ket and instance
     * @param key key of command (note: you can replace the old value, be careful)
     * @param command instance of command
     */
    public void addAction(String key, T command){
        T oldCommand;
        if ((oldCommand = actionsMap.put(key, command)) != null){
            log.warn("Action " + oldCommand.getClass().getName() + " replaced by " + command.getClass().getName() + " for key " + key);
        }
    }

    /**
     * Add all inner {@link Command}-classes from provided instance
     * @param obj instance of class which contains classes annotated by {@link Command}
     * @param clazz class-mark of implemented interface
     */
    public void addAction(Object obj, Class<T> clazz){
        Map<String, T> additionalActionMap = new CommandChain().init(obj, clazz).actionsMap;
        actionsMap.putAll(additionalActionMap);
    }

}
