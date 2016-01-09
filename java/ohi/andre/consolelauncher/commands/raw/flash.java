package ohi.andre.consolelauncher.commands.raw;

import android.hardware.Camera.Parameters;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecInfo;

@SuppressWarnings("deprecation")
public class flash implements CommandAbstraction {

	@Override
	public String exec(ExecInfo info) {
		if(!info.canUseFlash)
			return info.res.getString(R.string.output_flashlightnotavailable);
		
		if(info.camera == null)
			info.initCamera();
		
		if(!info.isFlashOn) {
			info.parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			info.camera.setParameters(info.parameters);
			info.camera.startPreview();
			info.isFlashOn = true;
			return info.res.getString(R.string.output_flashon);
		} else {
	        info.parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
	        info.camera.setParameters(info.parameters);
	        info.camera.stopPreview();
	        info.isFlashOn = false;
	        return info.res.getString(R.string.output_flashoff);
		}
	}

	@Override
	public int helpRes() {
		return R.string.help_flash;
	}

	@Override
	public int minArgs() {
		return 0;
	}

	@Override
	public int maxArgs() {
		return 0;
	}

	@Override
	public boolean useRealTime() {
		return true;
	}

	@Override
	public int[] argType() {
		return null;
	}

	@Override
	public int priority() {
		return 4;
	}

	@Override
	public String[] parameters() {
		return null;
	}

    @Override
    public String onNotArgEnough(ExecInfo info) {
        return null;
    }

	@Override
	public int notFoundRes() {
		return 0;
	}

}
