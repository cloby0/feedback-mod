package io.github.cloby0.feedback.compat.jade;

import io.github.cloby0.feedback.Feedback;
import io.github.cloby0.feedback.core.rotation.RotationNode;
import io.github.cloby0.feedback.machine.hammer.MechanicalHammerBlock;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import net.minecraft.world.level.block.Block;

/**
 * Hooks the mod's readouts onto Jade's HUD.
 *
 * <h2>Jade, carefully</h2>
 * A HUD that printed {@code 14.3 RPM} on a bare shaft would hand over the instrument layer for
 * free and gut philosophy 8. One that says <em>turning slowly</em> until an instrument is installed
 * is not a compromise with that rule, it <em>is</em> that rule, on a HUD. See the individual
 * providers for which lines are free and why.
 * <p>
 * This class is loaded only by Jade's own plugin scan, so nothing here runs when Jade is absent.
 */
@WailaPlugin(Feedback.MOD_ID)
public class FeedbackJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(RotationServerData.INSTANCE, RotationNode.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Registered against Block rather than each rotating block in turn: what makes a block
        // rotate is its block entity, not its class, and a list here would silently miss the next
        // machine somebody adds. The provider checks the block entity itself.
        registration.registerBlockComponent(RotationComponent.INSTANCE, Block.class);
        registration.registerBlockComponent(HammerComponent.INSTANCE, MechanicalHammerBlock.class);
    }
}
