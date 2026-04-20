window.PageFeedback = {
  getFriendlyError(error, fallback) {
    const message = error?.response?.data?.msg || error?.response?.data?.message || error?.message;
    if (message && !/^\d+$/.test(String(message).trim())) {
      return message;
    }
    if (error?.response) {
      return fallback;
    }
    return '网络异常，请检查网络后重试';
  },

  unwrapResult(response, fallback = '操作失败，请稍后重试') {
    const body = response?.data;
    const ok = body && (body.code === 200 || body.code === 0 || body.success === true);
    if (!ok) {
      throw new Error(body?.msg || body?.message || fallback);
    }
    return body.data;
  },

  successText(scene) {
    return `${scene}成功`;
  },

  failureText(scene) {
    return `${scene}失败，请稍后重试`;
  },

  alertSuccess(scene) {
    alert(this.successText(scene));
  },

  alertFailure(error, scene) {
    alert(this.getFriendlyError(error, this.failureText(scene)));
  }
};
