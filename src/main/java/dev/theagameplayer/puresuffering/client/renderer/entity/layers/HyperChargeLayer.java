package dev.theagameplayer.puresuffering.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.world.entity.PSHyperCharge;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public final class HyperChargeLayer<LE extends LivingEntity, EM extends EntityModel<LE>> extends RenderLayer<LE, EM> {
	private static final ResourceLocation HYPER_CHARGE_LOCATION = new ResourceLocation(PureSufferingMod.MODID, "textures/entity/charged_armor.png");
	private static final ResourceLocation HYPER_CHARGE_LOCATION2 = new ResourceLocation(PureSufferingMod.MODID, "textures/entity/charged_armor2.png");
	private final EM model;
	private final int maxHyperCharge;

	public HyperChargeLayer(final LivingEntityRenderer<LE, EM> rendererIn, final int maxHyperChargeIn) {
		super(rendererIn);
		this.model = rendererIn.getModel();
		this.maxHyperCharge = maxHyperChargeIn;
	}

	@Override
	public final void render(final PoseStack poseStackIn, final MultiBufferSource bufferIn, final int packedLightIn, final LE entityIn, final float limbSwingIn, final float limbSwingAmountIn, final float partialTicksIn, final float ageInTicksIn, final float netHeadYawIn, final float headPitchIn) {
		if (entityIn instanceof PSHyperCharge && ((PSHyperCharge)entityIn).psGetHyperCharge() > 0) {
			int hyperCharge = ((PSHyperCharge)entityIn).psGetHyperCharge();
			float tick = (float)entityIn.tickCount + partialTicksIn;
			EntityModel<LE> entityModel = this.model;
			entityModel.prepareMobModel(entityIn, limbSwingIn, limbSwingAmountIn, partialTicksIn);
			this.getParentModel().copyPropertiesTo(entityModel);
			VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.energySwirl(hyperCharge == this.maxHyperCharge ? HYPER_CHARGE_LOCATION2 : HYPER_CHARGE_LOCATION, (tick * (0.01F * hyperCharge)) % 1.0F, (tick * (0.01F * hyperCharge)) % 1.0F));
			entityModel.setupAnim(entityIn, limbSwingIn, limbSwingAmountIn, ageInTicksIn, netHeadYawIn, headPitchIn);
			entityModel.renderToBuffer(poseStackIn, vertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, 0.2F * hyperCharge, 0.2F * hyperCharge, 0.2F * hyperCharge, 0.2F * hyperCharge);
		}
	}
}
