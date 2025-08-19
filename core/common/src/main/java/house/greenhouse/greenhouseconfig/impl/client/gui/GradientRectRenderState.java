package house.greenhouse.greenhouseconfig.impl.client.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
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

	@Override
	public void buildVertices(VertexConsumer p_415536_, float p_418064_) {
		switch (this.direction()) {
			case LEFT_TO_RIGHT -> {
				p_415536_.addVertexWith2DPose(this.pose(), this.minX(), this.minY(), p_418064_).setColor(this.colorStart());
				p_415536_.addVertexWith2DPose(this.pose(), this.minX(), this.maxY(), p_418064_).setColor(this.colorStart());
				p_415536_.addVertexWith2DPose(this.pose(), this.maxX(), this.maxY(), p_418064_).setColor(this.colorEnd());
				p_415536_.addVertexWith2DPose(this.pose(), this.maxX(), this.minY(), p_418064_).setColor(this.colorEnd());
			}
			case TOP_TO_BOTTOM -> {
				p_415536_.addVertexWith2DPose(this.pose(), this.minX(), this.minY(), p_418064_).setColor(this.colorStart());
				p_415536_.addVertexWith2DPose(this.pose(), this.minX(), this.maxY(), p_418064_).setColor(this.colorEnd());
				p_415536_.addVertexWith2DPose(this.pose(), this.maxX(), this.maxY(), p_418064_).setColor(this.colorEnd());
				p_415536_.addVertexWith2DPose(this.pose(), this.maxX(), this.minY(), p_418064_).setColor(this.colorStart());
			}
		}
	}

	@Nullable
	private static ScreenRectangle getBounds(
			int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea
	) {
		ScreenRectangle screenrectangle = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
		return scissorArea != null ? scissorArea.intersection(screenrectangle) : screenrectangle;
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
