package com.hrznstudio.equilibrium.install.locator.windows;

import com.hrznstudio.equilibrium.install.locator.PotentialInstallLocator;
import org.apache.tools.ant.taskdefs.condition.Os;

public abstract class WindowsInstallLocator implements PotentialInstallLocator {
    @Override
    public boolean shouldUse() {
        return Os.isFamily(Os.FAMILY_WINDOWS);
    }
}
