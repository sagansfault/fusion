package com.projecki.fusion.menu.deprecated.lib;

import com.projecki.fusion.menu.deprecated.function.ButtonFunction;

/**
 * @deprecated use new v2 package
 */
@Deprecated
public interface ConfirmAction {

    void onConfirm(ButtonFunction.ClickInfo clickInfo);

    void onDeny(ButtonFunction.ClickInfo clickInfo);
}
