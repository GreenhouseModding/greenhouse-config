package house.greenhouse.greenhouseconfig.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import house.greenhouse.greenhouseconfig.impl.util.Duck_ColoredRectangleRenderState;
import house.greenhouse.greenhouseconfig.impl.util.Duck_GuiGraphics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.ColoredRectangleRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiGraphics.class)
public final class Mixin_GuiGraphics implements Duck_GuiGraphics {
	@Unique
	private boolean greenhouseconfig$horizontalGradient = false;

	private Mixin_GuiGraphics() {}

	@Override
	public void greenhouseconfig$horizontalGradient() {
		this.greenhouseconfig$horizontalGradient = true;
	}

	@Override
	public void greenhouseconfig$verticalGradient() {
		this.greenhouseconfig$horizontalGradient = false;
	}

	@WrapOperation(
			method = "submitColoredRectangle",
			at = @At(value = "NEW", target = "(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;Lorg/joml/Matrix3x2f;IIIIIILnet/minecraft/client/gui/navigation/ScreenRectangle;)Lnet/minecraft/client/gui/render/state/ColoredRectangleRenderState;")
	)
	private ColoredRectangleRenderState rotateColor(
			RenderPipeline pipeline,
			TextureSetup textureSetup,
			Matrix3x2f pose,
			int minX,
			int minY,
			int maxX,
			int maxY,
			int colorStart,
			int colorEnd,
			@Nullable ScreenRectangle scissorArea,
			Operation<ColoredRectangleRenderState> original
	) {
		var renderState = original.call(pipeline, textureSetup, pose, minX, minY, maxX, maxY, colorStart, colorEnd, scissorArea);
		((Duck_ColoredRectangleRenderState) (Object) renderState).greenhouseconfig$setHorizontalGradient(this.greenhouseconfig$horizontalGradient);

		return renderState;
	}
}
