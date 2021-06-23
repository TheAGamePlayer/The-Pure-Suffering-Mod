function initializeCoreMod() {
	return {
		'client_day_brightness': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.client.world.ClientWorld',
				'methodName': 'func_228326_g_',
				'methodDesc': '(F)F'
			},
			'transformer': function(method) {
				var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
				ASMAPI.log('INFO', 'Adding \'client_day_brightness\' ASM patch...');
				var Opcodes = Java.type('org.objectweb.asm.Opcodes');
				var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
				var fReturn = ASMAPI.findFirstInstructionAfter(method, Opcodes.FRETURN, 0);
				while (fReturn !== null) {
					method.instructions.insertBefore(fReturn, new VarInsnNode(Opcodes.ALOAD, 0));
					method.instructions.insertBefore(fReturn, ASMAPI.buildMethodCall(
						'dev/theagameplayer/puresuffering/coremod/PSCoreModHandler',
						'clientDayBrightness',
						"(FLnet/minecraft/world/World;)F",
						ASMAPI.MethodType.STATIC));
					fReturn = ASMAPI.findFirstInstructionAfter(method, Opcodes.FRETURN, method.instructions.indexOf(fReturn) + 2);
				}
				ASMAPI.log('INFO', 'Added \'client_day_brightness\' ASM patch!');
				return method;
			}
		}
	}
}