package house.greenhouse.greenhouseconfig.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import house.greenhouse.greenhouseconfig.impl.util.Duck_ColoredRectangleRenderState;
import net.minecraft.client.gui.render.state.ColoredRectangleRenderState;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ColoredRectangleRenderState.class)
public abstract class Mixin_ColoredRectangleRenderState implements Duck_ColoredRectangleRenderState {
	@Unique
	private boolean greenhouseconfig$horizontalGradient;

	private Mixin_ColoredRectangleRenderState() {}

	@Override
	public void greenhouseconfig$setHorizontalGradient(boolean horizontalGradient) {
		this.greenhouseconfig$horizontalGradient = horizontalGradient;
	}

	@Shadow
	public abstract int col1();

	@Shadow
	public abstract int col2();

	@WrapOperation(
			method = "buildVertices",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(I)Lcom/mojang/blaze3d/vertex/VertexConsumer;", ordinal = 1)
	)
	private VertexConsumer rotateColor1(VertexConsumer instance, int color, Operation<VertexConsumer> original) {
		if (this.greenhouseconfig$horizontalGradient) {
			return original.call(instance, this.col1());
		} else {
			return original.call(instance, color);
		}
	}

	@WrapOperation(
			method = "buildVertices",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(I)Lcom/mojang/blaze3d/vertex/VertexConsumer;", ordinal = 3)
	)
	private VertexConsumer rotateColor3(VertexConsumer instance, int color, Operation<VertexConsumer> original) {
		if (this.greenhouseconfig$horizontalGradient) {
			return original.call(instance, this.col2());
		} else {
			return original.call(instance, color);
		}
	}
}
