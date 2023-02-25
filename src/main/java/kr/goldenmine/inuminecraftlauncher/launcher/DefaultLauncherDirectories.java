package kr.goldenmine.inuminecraftlauncher.launcher;

import java.io.File;

public class DefaultLauncherDirectories implements LauncherDirectories {
    private final File workDir;

    public DefaultLauncherDirectories(File rootDir) {
        workDir = rootDir;
    }

    public File getLauncherDirectory() {
        if (!workDir.exists())
            workDir.mkdirs();

        return workDir;
    }

    public File getLibrariesDirectory() {
        File cache = new File(getLauncherDirectory(), "libraries");
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return cache;
    }

    public File getAssetsDirectory() {
        File assets = new File(getLauncherDirectory(), "assets");

        if (!assets.exists()) {
            assets.mkdirs();
        }

        return assets;
    }

    @Override
    public File getForgeDirectory() {
        File forge = new File(getLauncherDirectory(), "forge");

        if (!forge.exists()) {
            forge.mkdirs();
        }

        return forge;
    }

    @Override
    public File getInstancesDirectory() {
        File instances = new File(getLauncherDirectory(), "instances");

        if (!instances.exists())
            instances.mkdirs();

        return instances;
    }

    @Override
    public File getJavaDirectory() {
        File runtimes = new File(getLauncherDirectory(), "java");

        if (!runtimes.exists())
            runtimes.mkdirs();

        return runtimes;
    }

    @Override
    public File getTemporaryDirectory() {
        File temporary = new File(getLauncherDirectory(), "temp");
        if (!temporary.exists()) {
            temporary.mkdirs();
        }
        return temporary;
    }
}