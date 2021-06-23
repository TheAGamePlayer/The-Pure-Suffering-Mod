function initializeCoreMod() {
	return {
		'server_day_brightness': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.world.World',
				'methodName': 'func_72966_v',
				'methodDesc': '()V'
			},
			'transformer': function(method) {
				var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
				ASMAPI.log('INFO', 'Adding \'server_day_brightness\' ASM patch...');
				var Opcodes = Java.type('org.objectweb.asm.Opcodes');
				var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
				var putField = ASMAPI.findFirstInstructionAfter(method, Opcodes.PUTFIELD, 0);
				while (putField !== null) {
					method.instructions.insertBefore(putField, new VarInsnNode(Opcodes.ALOAD, 0));
					method.instructions.insertBefore(putField, ASMAPI.buildMethodCall(
						'dev/theagameplayer/puresuffering/coremod/PSCoreModHandler',
						'serverDayBrightness',
						"(ILnet/minecraft/world/World;)I",
						ASMAPI.MethodType.STATIC));
					putField = ASMAPI.findFirstInstructionAfter(method, Opcodes.PUTFIELD, method.instructions.indexOf(putField) + 2);
				}
				ASMAPI.log('INFO', 'Added \'server_day_brightness\' ASM patch!');
				return method;
			}
		}
	}
}