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
	
	public HyperChargeLayer(final MobRenderer<M, EM> pRenderer) {
		super(pRenderer);
		this.model = pRenderer.getModel();
	}

	@Override
	public final void render(final PoseStack pPoseStack, final MultiBufferSource pBuffer, final int pPackedLight, final M pMob, final float pLimbSwing, final float pLimbSwingAmount, final float pPartialTicks, final float pAgeInTicks, final float pNetHeadYaw, final float pHeadPitch) {
		if (pMob instanceof PSInvasionMob invasionMob && invasionMob.psGetHyperCharge() > 0) {
			final EntityModel<M> entityModel = this.model;
			final int hyperCharge = invasionMob.psGetHyperCharge();
			final float tick = (float)pMob.tickCount + pPartialTicks;
			final float argb = 0.2F * hyperCharge;
			entityModel.prepareMobModel(pMob, pLimbSwing, pLimbSwingAmount, pPartialTicks);
			this.getParentModel().copyPropertiesTo(entityModel);
			final VertexConsumer vertexConsumer = pBuffer.getBuffer(RenderType.energySwirl(hyperCharge < 4 ? HYPER_CHARGE_LOCATION1 : HYPER_CHARGE_LOCATION2, Mth.sin(tick * 0.007854F * hyperCharge), tick * (0.01F * hyperCharge) % 1.0F));
			entityModel.setupAnim(pMob, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
			entityModel.renderToBuffer(pPoseStack, vertexConsumer, pPackedLight, OverlayTexture.NO_OVERLAY, argb, argb, argb, argb);
		}
	}
}
