package ir.vasfa.iman.edgetoolkit.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.CubeGrid;
import com.github.ybq.android.spinkit.style.DoubleBounce;

import ir.vasfa.iman.edgetoolkit.MainActivity;
import ir.vasfa.iman.edgetoolkit.R;

public class LoadingDialog {

    Dialog alertDialogBuilder;

    public LoadingDialog(Activity activity) {
        alertDialogBuilder = new Dialog(activity);
        final LayoutInflater inflater = activity.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.loading_dialog, null);

        ProgressBar progressBar = (ProgressBar)dialogView.findViewById(R.id.spin_kit);
        CubeGrid doubleBounce = new CubeGrid();
        progressBar.setIndeterminateDrawable(doubleBounce);

        alertDialogBuilder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialogBuilder.setContentView(dialogView);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogBuilder.show();

//        if (activity instanceof MainActivity){
//            ((MainActivity) activity).editHandler();
//        }

    }

    public void diss() {
        alertDialogBuilder.dismiss();
    }

}
