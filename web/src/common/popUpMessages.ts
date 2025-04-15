import emiter from '../boot/appbus';

interface ISnackData {
  success: boolean;
  message: string;
}

interface IConfirmDialogData {
  title: string;
  message: string;
  actionOK: () => void;
}

interface IProgressData {
  state: boolean;
  title: string;
  message: string;
}

function showSnack(success: boolean, message: string) {
  emiter.emit('show_snack', { success, message } as ISnackData);
}

function ConfirmDialog(title: string, message: string, actionOK: () => void) {
  emiter.emit('show_confirm', { title, message, actionOK });
}

function ProcessDilog(state: boolean, title = '', message = '') {
  emiter.emit('show_process', { state, title, message });
}

export { showSnack, ConfirmDialog, ProcessDilog };
export type { ISnackData, IConfirmDialogData, IProgressData };
