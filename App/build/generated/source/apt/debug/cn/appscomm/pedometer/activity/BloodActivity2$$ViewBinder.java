// Generated code from Butter Knife. Do not modify!
package cn.appscomm.pedometer.activity;

import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Finder;
import butterknife.internal.ViewBinder;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;

public class BloodActivity2$$ViewBinder<T extends BloodActivity2> implements ViewBinder<T> {
  @Override
  public Unbinder bind(final Finder finder, final T target, Object source) {
    InnerUnbinder unbinder = createUnbinder(target);
    View view;
    view = finder.findRequiredView(source, 2131624055, "field 'btnBack' and method 'onClick'");
    target.btnBack = finder.castView(view, 2131624055, "field 'btnBack'");
    unbinder.view2131624055 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick(p0);
      }
    });
    view = finder.findRequiredView(source, 2131624000, "field 'title'");
    target.title = finder.castView(view, 2131624000, "field 'title'");
    view = finder.findRequiredView(source, 2131624057, "field 'ivPagePre' and method 'onClick'");
    target.ivPagePre = finder.castView(view, 2131624057, "field 'ivPagePre'");
    unbinder.view2131624057 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick(p0);
      }
    });
    view = finder.findRequiredView(source, 2131624058, "field 'bloodDate'");
    target.bloodDate = finder.castView(view, 2131624058, "field 'bloodDate'");
    view = finder.findRequiredView(source, 2131624059, "field 'ivPageNext' and method 'onClick'");
    target.ivPageNext = finder.castView(view, 2131624059, "field 'ivPageNext'");
    unbinder.view2131624059 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick(p0);
      }
    });
    view = finder.findRequiredView(source, 2131624056, "field 'changePage'");
    target.changePage = finder.castView(view, 2131624056, "field 'changePage'");
    view = finder.findRequiredView(source, 2131624062, "field 'bloodValueShou'");
    target.bloodValueShou = finder.castView(view, 2131624062, "field 'bloodValueShou'");
    view = finder.findRequiredView(source, 2131624061, "field 'r2'");
    target.r2 = finder.castView(view, 2131624061, "field 'r2'");
    view = finder.findRequiredView(source, 2131624064, "field 'bloodValueSu'");
    target.bloodValueSu = finder.castView(view, 2131624064, "field 'bloodValueSu'");
    view = finder.findRequiredView(source, 2131624063, "field 'r3'");
    target.r3 = finder.castView(view, 2131624063, "field 'r3'");
    view = finder.findRequiredView(source, 2131624060, "field 'bloodContainer'");
    target.bloodContainer = finder.castView(view, 2131624060, "field 'bloodContainer'");
    view = finder.findRequiredView(source, 2131624066, "field 'left'");
    target.left = view;
    view = finder.findRequiredView(source, 2131624067, "field 'right'");
    target.right = view;
    view = finder.findRequiredView(source, 2131624065, "field 'indicitater'");
    target.indicitater = finder.castView(view, 2131624065, "field 'indicitater'");
    view = finder.findRequiredView(source, 2131624068, "field 'bloodViewpager'");
    target.bloodViewpager = finder.castView(view, 2131624068, "field 'bloodViewpager'");
    view = finder.findRequiredView(source, 2131624069, "field 'doing' and method 'onClick'");
    target.doing = finder.castView(view, 2131624069, "field 'doing'");
    unbinder.view2131624069 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick(p0);
      }
    });
    return unbinder;
  }

  protected InnerUnbinder<T> createUnbinder(T target) {
    return new InnerUnbinder(target);
  }

  protected static class InnerUnbinder<T extends BloodActivity2> implements Unbinder {
    private T target;

    View view2131624055;

    View view2131624057;

    View view2131624059;

    View view2131624069;

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
      view2131624055.setOnClickListener(null);
      target.btnBack = null;
      target.title = null;
      view2131624057.setOnClickListener(null);
      target.ivPagePre = null;
      target.bloodDate = null;
      view2131624059.setOnClickListener(null);
      target.ivPageNext = null;
      target.changePage = null;
      target.bloodValueShou = null;
      target.r2 = null;
      target.bloodValueSu = null;
      target.r3 = null;
      target.bloodContainer = null;
      target.left = null;
      target.right = null;
      target.indicitater = null;
      target.bloodViewpager = null;
      view2131624069.setOnClickListener(null);
      target.doing = null;
    }
  }
}
