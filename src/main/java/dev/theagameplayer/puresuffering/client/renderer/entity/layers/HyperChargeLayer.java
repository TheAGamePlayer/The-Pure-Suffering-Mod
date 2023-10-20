package dev.theagameplayer.puresuffering.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.world.entity.PSInvasionMob;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

public final class HyperChargeLayer<M extends Mob, EM extends EntityModel<M>> extends RenderLayer<M, EM> {
	private static final ResourceLocation HYPER_CHARGE_LOCATION1 = PureSufferingMod.namespace("textures/entity/charged_armor1.png");
	private static final ResourceLocation HYPER_CHARGE_LOCATION2 = PureSufferingMod.namespace("textures/entity/charged_armor2.png");
	private final EM model;
	
	public HyperChargeLayer(final MobRenderer<M, EM> rendererIn) {
		super(rendererIn);
		this.model = rendererIn.getModel();
	}

	@Override
	public final void render(final PoseStack poseStackIn, final MultiBufferSource bufferIn, final int packedLightIn, final M mobIn, final float limbSwingIn, final float limbSwingAmountIn, final float partialTicksIn, final float ageInTicksIn, final float netHeadYawIn, final float headPitchIn) {
		if (mobIn instanceof PSInvasionMob invasionMob && invasionMob.psGetHyperCharge() > 0) {
			final EntityModel<M> entityModel = this.model;
			final int hyperCharge = invasionMob.psGetHyperCharge();
			final float tick = (float)mobIn.tickCount + partialTicksIn;
			final float argb = 0.2F * hyperCharge;
			entityModel.prepareMobModel(mobIn, limbSwingIn, limbSwingAmountIn, partialTicksIn);
			this.getParentModel().copyPropertiesTo(entityModel);
			final VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.energySwirl(hyperCharge < 4 ? HYPER_CHARGE_LOCATION1 : HYPER_CHARGE_LOCATION2, Mth.sin(tick * 0.007854F * hyperCharge), tick * (0.01F * hyperCharge) % 1.0F));
			entityModel.setupAnim(mobIn, limbSwingIn, limbSwingAmountIn, ageInTicksIn, netHeadYawIn, headPitchIn);
			entityModel.renderToBuffer(poseStackIn, vertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, argb, argb, argb, argb);
		}
	}
}
