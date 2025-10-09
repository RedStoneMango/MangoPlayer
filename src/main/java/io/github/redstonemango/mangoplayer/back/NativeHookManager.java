/*
    --- THIS CLASS WAS USED FOR IMPLEMENTING jNativeHook INTO THE APPLICATION TO ALLOW FOR PAUSING THE SONG WHILE USING THE FLOATING SONG CONTROL              ---
    --- HOWEVER, DUE TO CRITICAL ISSUES WITH KEY CODE DETECTION ON SPECIFIC OPERATING SYSTEMS and DISPLAY MANAGERS, THIS CLASS HAS BEEN EVENTUALLY DEACTIVATED ---
 */
package io.github.redstonemango.mangoplayer.back;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import io.github.redstonemango.mangoplayer.front.MangoPlayer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NativeHookManager implements NativeKeyListener {
    private static NativeHookManager instance;

    private @Nullable NativeKeyListener child = null;

    private NativeHookManager() {}

    public static synchronized NativeHookManager getInstance() {
        if (instance == null) {
            instance = new NativeHookManager();

            // Initialize jNativeHook
            System.setProperty("jnativehook.lib.path", MangoPlayer.APP_FOLDER_PATH + "/internalData/nativeLibs/");
            System.out.println("Updated environment variable 'jnativehook.lib.path' to " + System.getProperty("jnativehook.lib.path", "VALUE NOT EXISTING"));
            new File(MangoPlayer.APP_FOLDER_PATH + "/internalData/nativeLibs/").mkdirs();
            if (!GlobalScreen.isNativeHookRegistered()) {
                System.out.println("Registering native hook...");
                try {
                    GlobalScreen.registerNativeHook();
                } catch (NativeHookException e) {
                    throw new RuntimeException("Unable to register native hook!", e);
                }
                System.out.println("Done registering native hook!");
                GlobalScreen.addNativeKeyListener(instance);

                Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
                logger.setUseParentHandlers(false);
                logger.setLevel(Level.WARNING);

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("Unregistering native hook...");
                    GlobalScreen.removeNativeKeyListener(instance);
                    try {
                        GlobalScreen.unregisterNativeHook();
                    }
                    catch (NativeHookException e) {
                        System.err.println("Unable to unregister native hook: " + e);
                    }
                    System.out.println("Done unregistering native hook!");
                }, "native hook shutdown thread"));
            }
        }
        return instance;
    }

    public synchronized void setChild(@Nullable NativeKeyListener child) {
        this.child = child;
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        if (child != null) child.nativeKeyTyped(e);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (child != null) child.nativeKeyPressed(e);
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (child != null) child.nativeKeyReleased(e);
    }
}
