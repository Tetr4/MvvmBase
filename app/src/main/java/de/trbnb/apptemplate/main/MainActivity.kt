package de.trbnb.apptemplate.main

import android.app.AlertDialog
import android.app.Dialog
import android.support.design.widget.Snackbar
import de.trbnb.apptemplate.BR
import de.trbnb.apptemplate.R
import de.trbnb.mvvmbase.MvvmActivity
import org.koin.android.architecture.ext.getViewModel
import javax.inject.Provider

class MainActivity : MvvmActivity<MainViewModel>() {

    private var dialog: Dialog? = null
    private var snackbar: Snackbar? = null

    override val layoutId = R.layout.activity_main
    override val viewModelProvider = Provider { getViewModel<MainViewModel>() }

    override fun onViewModelLoaded(viewModel: MainViewModel) {
        super.onViewModelLoaded(viewModel)

        // now that the view-model is loaded, we know whether or not we should know if we should
        // show the dialog and snackbar
        arrayOf(BR.showingDialog, BR.showSnackbar).forEach { onViewModelPropertyChanged(viewModel, it) }
    }

    /**
     * will be called whenever the view-model calls notifyPropertyChanged
     */
    override fun onViewModelPropertyChanged(viewModel: MainViewModel, fieldId: Int) {
        when(fieldId){
            BR.showingDialog -> if(viewModel.isShowingDialog) showDialog() else dismissDialog()
            BR.showSnackbar -> if(viewModel.showSnackbar) showSnackbar() else dismissSnackbar()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        //Dialogs should be dismissed in onDestroy so the window won't be leaked.
        //This means we don't want to change the state in the view-model here.

        //We dismiss and don't cancel here because we don't want to trigger the OnCancelListener that
        //is attached to the dialog.
        dialog?.dismiss()
    }

    /**
     * create a new Dialog and show it
     */
    private fun showDialog(){
        dialog = AlertDialog.Builder(this)
                .setTitle("Dialog title")
                .setMessage("This is a sample dialog to show how to create a dialog via binding.")
                .setOnCancelListener {
                    // Will only be called when the Dialog is canceled, not dismissed.
                    // This also changes the state in the view-model, so the dialog won't be shown
                    // again after rotation.
                    viewModel?.isShowingDialog = false
                }
                .setPositiveButton(android.R.string.ok) { d, _ ->
                    // We cancel the Dialog here because the user got rid of it.
                    d.cancel()
                }
                .show()
    }

    /**
     * dismiss a Dialog if one exists
     */
    private fun dismissDialog(){
        dialog?.dismiss()
        dialog = null
    }

    /**
     * create a new Snackbar and show it
     */
    private fun showSnackbar(){
        snackbar = Snackbar.make(findViewById(android.R.id.content), "This is a sample Snackbar made with binding.", Snackbar.LENGTH_LONG).apply {

            // if the snackbar is dismissed we want to update the state in the view-model
            addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    //A Snackbar will dismiss itself if the containing Activity is destroyed.
                    //Because we don't want to change the state in the view-model we just return in that case.
                    if(isDestroyed){
                        return
                    }

                    viewModel?.showSnackbar = false
                }
            })

            setAction("Hide") { viewModel?.showSnackbar = false }

            show()
        }
    }

    /**
     * dismiss a Snackbar if one exists
     */
    private fun dismissSnackbar(){
        snackbar?.dismiss()
        snackbar = null
    }

}
