// Generated code from Butter Knife. Do not modify!
package cn.appscomm.pedometer.avater;

import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.Finder;
import butterknife.internal.ViewBinder;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;

public class BloodFragment$$ViewBinder<T extends BloodFragment> implements ViewBinder<T> {
  @Override
  public Unbinder bind(final Finder finder, final T target, Object source) {
    InnerUnbinder unbinder = createUnbinder(target);
    View view;
    view = finder.findRequiredView(source, 2131624645, "field 'bloodListview'");
    target.bloodListview = finder.castView(view, 2131624645, "field 'bloodListview'");
    view = finder.findRequiredView(source, 2131624646, "field 'linechart'");
    target.linechart = finder.castView(view, 2131624646, "field 'linechart'");
    view = finder.findRequiredView(source, 2131624647, "field 'nodatatext'");
    target.nodatatext = finder.castView(view, 2131624647, "field 'nodatatext'");
    return unbinder;
  }

  protected InnerUnbinder<T> createUnbinder(T target) {
    return new InnerUnbinder(target);
  }

  protected static class InnerUnbinder<T extends BloodFragment> implements Unbinder {
    private T target;

    protected InnerUnbinder(T target) {
      this.target = target;
    }

    @Override
    public final void unbind() {
      if (target == null) throw new IllegalStateException("Bindings already cleared.");
      unbind(target);
      target = null;
    }

    protected void unbind(T target) {
      target.bloodListview = null;
      target.linechart = null;
      target.nodatatext = null;
    }
  }
}
