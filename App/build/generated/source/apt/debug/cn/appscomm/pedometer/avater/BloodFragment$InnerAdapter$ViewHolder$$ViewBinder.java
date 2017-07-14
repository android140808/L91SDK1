// Generated code from Butter Knife. Do not modify!
package cn.appscomm.pedometer.avater;

import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.Finder;
import butterknife.internal.ViewBinder;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;

public class BloodFragment$InnerAdapter$ViewHolder$$ViewBinder<T extends BloodFragment.InnerAdapter.ViewHolder> implements ViewBinder<T> {
  @Override
  public Unbinder bind(final Finder finder, final T target, Object source) {
    InnerUnbinder unbinder = createUnbinder(target);
    View view;
    view = finder.findRequiredView(source, 2131624516, "field 'time'");
    target.time = finder.castView(view, 2131624516, "field 'time'");
    view = finder.findRequiredView(source, 2131624517, "field 'hight'");
    target.hight = finder.castView(view, 2131624517, "field 'hight'");
    view = finder.findRequiredView(source, 2131624518, "field 'lower'");
    target.lower = finder.castView(view, 2131624518, "field 'lower'");
    view = finder.findRequiredView(source, 2131624519, "field 'value'");
    target.value = finder.castView(view, 2131624519, "field 'value'");
    return unbinder;
  }

  protected InnerUnbinder<T> createUnbinder(T target) {
    return new InnerUnbinder(target);
  }

  protected static class InnerUnbinder<T extends BloodFragment.InnerAdapter.ViewHolder> implements Unbinder {
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
      target.time = null;
      target.hight = null;
      target.lower = null;
      target.value = null;
    }
  }
}
