package com.projecki.fusion.user;

import com.projecki.fusion.object.Module;

/**
 * A {@link Module} for {@link User Users}.
 * <p>
 *     Implementations must be annotated with {@link UserModuleId}.
 * </p>
 *
 * @since May 27, 2022
 * @author Andavin
 */
public abstract class UserModule<U extends User> extends Module<U> {
}
