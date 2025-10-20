package house.greenhouse.greenhouseconfig.test.client.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record GradientRectRenderState(
		RenderPipeline pipeline,
		TextureSetup textureSetup,
		Matrix3x2f pose,
		int minX,
		int minY,
		int maxX,
		int maxY,
		int colorStart,
		int colorEnd,
		GradientDirection direction,
		@Nullable ScreenRectangle scissorArea,
		@Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
	public GradientRectRenderState(
			GuiGraphics graphics,
			int minX,
			int minY,
			int maxX,
			int maxY,
			int colorStart,
			int colorEnd,
			GradientDirection direction,
			@Nullable ScreenRectangle scissorArea
	) {
		this(
				RenderPipelines.GUI,
				TextureSetup.noTexture(),
				graphics.pose(),
				minX,
				minY,
				maxX,
				maxY,
				colorStart,
				colorEnd,
				direction,
				scissorArea,
				getBounds(minX, minY, maxX, maxY, graphics.pose(), scissorArea)
		);
	}

	@Nullable
	private static ScreenRectangle getBounds(
			int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea
	) {
		ScreenRectangle screenrectangle = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
		return scissorArea != null ? scissorArea.intersection(screenrectangle) : screenrectangle;
	}

	@Override
	public void buildVertices(VertexConsumer vertexConsumer) {
		switch (this.direction()) {
			case LEFT_TO_RIGHT -> {
				vertexConsumer.addVertexWith2DPose(this.pose(), this.minX(), this.minY()).setColor(this.colorStart());
				vertexConsumer.addVertexWith2DPose(this.pose(), this.minX(), this.maxY()).setColor(this.colorStart());
				vertexConsumer.addVertexWith2DPose(this.pose(), this.maxX(), this.maxY()).setColor(this.colorEnd());
				vertexConsumer.addVertexWith2DPose(this.pose(), this.maxX(), this.minY()).setColor(this.colorEnd());
			}
			case TOP_TO_BOTTOM -> {
				vertexConsumer.addVertexWith2DPose(this.pose(), this.minX(), this.minY()).setColor(this.colorStart());
				vertexConsumer.addVertexWith2DPose(this.pose(), this.minX(), this.maxY()).setColor(this.colorEnd());
				vertexConsumer.addVertexWith2DPose(this.pose(), this.maxX(), this.maxY()).setColor(this.colorEnd());
				vertexConsumer.addVertexWith2DPose(this.pose(), this.maxX(), this.minY()).setColor(this.colorStart());
			}
		}
	}

	public enum GradientDirection {
		LEFT_TO_RIGHT,
		TOP_TO_BOTTOM;
	}

	public static void fillGradient(
			GuiGraphics graphics,
			int minX,
			int minY,
			int maxX,
			int maxY,
			int colorStart,
			int colorEnd,
			GradientDirection direction
	) {
		graphics.guiRenderState.submitGuiElement(new GradientRectRenderState(
				graphics,
				minX,
				minY,
				maxX,
				maxY,
				colorStart,
				colorEnd,
				direction,
				graphics.scissorStack.peek()
		));
	}
}
