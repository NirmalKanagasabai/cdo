/*
 * Copyright (c) 2004-2014 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.releng.internal.setup.ui;

import org.eclipse.emf.cdo.releng.internal.setup.ui.BundlePoolAnalyzer.Artifact;
import org.eclipse.emf.cdo.releng.internal.setup.ui.BundlePoolAnalyzer.BundlePool;
import org.eclipse.emf.cdo.releng.internal.setup.ui.BundlePoolAnalyzer.Handler;
import org.eclipse.emf.cdo.releng.internal.setup.ui.BundlePoolAnalyzer.Profile;
import org.eclipse.emf.cdo.releng.setup.util.UIUtil;

import org.eclipse.net4j.util.ObjectUtil;
import org.eclipse.net4j.util.om.monitor.SubMonitor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Eike Stepper
 */
public class BundlePoolComposite extends Composite
{
  private static final int TABLE_STYLE = SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.NO_SCROLL | SWT.V_SCROLL;

  private final Set<ISelectionProvider> changingSelection = new HashSet<ISelectionProvider>();

  private TableViewer bundlePoolViewer;

  private BundlePoolContentProvider bundlePoolContentProvider;

  private TableViewer artifactViewer;

  private ArtifactContentProvider artifactContentProvider;

  private TableViewer profileViewer;

  private ProfileContentProvider profileContentProvider;

  private Button selectAllArtifactsButton;

  private Button repairArtifactsButton;

  private Button deleteArtifactsButton;

  // private Button deleteProfileButton;

  private BundlePoolAnalyzer analyzer;

  private BundlePool currentBundlePool;

  public BundlePoolComposite(final Composite parent, int style)
  {
    super(parent, style);
    GridLayout gridLayout = new GridLayout(1, false);
    gridLayout.marginWidth = 10;
    gridLayout.marginHeight = 10;
    setLayout(gridLayout);

    createBundlePoolViewer();

    SashForm verticalSashForm = new SashForm(this, SWT.VERTICAL);
    verticalSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    createArtifactViewer(verticalSashForm);
    createProfileViewer(verticalSashForm);

    verticalSashForm.setWeights(new int[] { 3, 1 });

    addDisposeListener(new DisposeListener()
    {
      public void widgetDisposed(DisposeEvent e)
      {
        if (analyzer != null)
        {
          analyzer.dispose();
        }
      }
    });

    final Shell shell = getShell();
    final Cursor oldCursor = shell.getCursor();
    shell.setCursor(new Cursor(getDisplay(), SWT.CURSOR_WAIT));
    setEnabled(false);

    getDisplay().asyncExec(new Runnable()
    {
      public void run()
      {
        initAgent();

        shell.setCursor(oldCursor);
        setEnabled(true);

        bundlePoolContentProvider.setInput(bundlePoolViewer, analyzer);
      }
    });
  }

  private void createBundlePoolViewer()
  {
    Composite bundlePoolComposite = new Composite(this, SWT.NONE);
    GridLayout bundlePoolLayout = new GridLayout(1, false);
    bundlePoolLayout.marginWidth = 0;
    bundlePoolLayout.marginHeight = 0;
    bundlePoolComposite.setLayout(bundlePoolLayout);
    bundlePoolComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

    bundlePoolViewer = new TableViewer(bundlePoolComposite, TABLE_STYLE);
    Table bundlePoolTable = bundlePoolViewer.getTable();
    bundlePoolTable.setHeaderVisible(true);
    GridData bundlePoolData = new GridData(SWT.FILL, SWT.FILL, true, false);
    bundlePoolData.heightHint = 84;
    bundlePoolTable.setLayoutData(bundlePoolData);

    TableColumn bundlePoolColumn = new TableViewerColumn(bundlePoolViewer, SWT.NONE).getColumn();
    bundlePoolColumn.setText("Bundle Pool");
    bundlePoolColumn.setWidth(305);
    bundlePoolColumn.setResizable(false);

    TableColumn artifactsColumn = new TableViewerColumn(bundlePoolViewer, SWT.NONE).getColumn();
    artifactsColumn.setText("Artifacts");
    artifactsColumn.setAlignment(SWT.RIGHT);
    artifactsColumn.setWidth(63);

    TableColumn unusedArtifactsColumn = new TableViewerColumn(bundlePoolViewer, SWT.NONE).getColumn();
    unusedArtifactsColumn.setText("Unused Artifacts");
    unusedArtifactsColumn.setAlignment(SWT.RIGHT);
    unusedArtifactsColumn.setWidth(105);

    TableColumn damagedArtifactsColumn = new TableViewerColumn(bundlePoolViewer, SWT.NONE).getColumn();
    damagedArtifactsColumn.setText("Damaged Artifacts");
    damagedArtifactsColumn.setAlignment(SWT.RIGHT);
    damagedArtifactsColumn.setWidth(125);

    TableColumn profilesColumn = new TableViewerColumn(bundlePoolViewer, SWT.NONE).getColumn();
    profilesColumn.setText("Profiles");
    profilesColumn.setAlignment(SWT.RIGHT);
    profilesColumn.setWidth(55);

    bundlePoolContentProvider = new BundlePoolContentProvider();
    bundlePoolViewer.setContentProvider(bundlePoolContentProvider);
    bundlePoolViewer.setLabelProvider(new TableLabelProvider(getDisplay()));
    bundlePoolViewer.addSelectionChangedListener(new SelectionChangedListener()
    {
      @Override
      protected void doSelectionChanged(SelectionChangedEvent event)
      {
        currentBundlePool = (BundlePool)((IStructuredSelection)event.getSelection()).getFirstElement();

        artifactContentProvider.setInput(artifactViewer, currentBundlePool);
        artifactViewer.setSelection(StructuredSelection.EMPTY);
        updateArtifactButtons();

        profileContentProvider.setInput(profileViewer, currentBundlePool);
        profileViewer.setSelection(StructuredSelection.EMPTY);

        // bundlePoolMoveButton.setEnabled(true);
        // bundlePoolDeleteButton.setEnabled(true);

        // updateProfileButtons(currentBundlePool);
      }
    });

    // Composite composite = new Composite(composite_3, SWT.NONE);
    // composite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false));
    // GridLayout gl_composite = new GridLayout(2, false);
    // gl_composite.marginWidth = 0;
    // gl_composite.marginHeight = 0;
    // composite.setLayout(gl_composite);
    //
    // final Button bundlePoolMoveButton = new Button(composite, SWT.NONE);
    // bundlePoolMoveButton.setText("Move...");
    // bundlePoolMoveButton.setEnabled(false);
    // bundlePoolMoveButton.addSelectionListener(new SelectionAdapter()
    // {
    // @Override
    // public void widgetSelected(SelectionEvent e)
    // {
    // moveBundlePool(getCurrentBundlePool());
    // }
    // });
    //
    // final Button bundlePoolDeleteButton = new Button(composite, SWT.NONE);
    // bundlePoolDeleteButton.setText("Delete All Artifacts...");
    // bundlePoolDeleteButton.setEnabled(false);
    // bundlePoolDeleteButton.addSelectionListener(new SelectionAdapter()
    // {
    // @Override
    // public void widgetSelected(SelectionEvent e)
    // {
    // deleteBundlePool(getCurrentBundlePool());
    // }
    // });

    new Label(bundlePoolComposite, SWT.NONE);
  }

  private void createArtifactViewer(Composite parent)
  {
    Composite artifactComposite = new Composite(parent, SWT.NONE);
    GridLayout artifactLayout = new GridLayout(1, false);
    artifactLayout.marginWidth = 0;
    artifactLayout.marginHeight = 0;
    artifactComposite.setLayout(artifactLayout);

    artifactViewer = new TableViewer(artifactComposite, TABLE_STYLE | SWT.MULTI);
    Table artifactTable = artifactViewer.getTable();
    artifactTable.setHeaderVisible(true);
    artifactTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    new SelectAllAdapter(artifactViewer);

    TableColumn artifactColumn = new TableViewerColumn(artifactViewer, SWT.NONE).getColumn();
    artifactColumn.setText("Artifact");
    artifactColumn.setWidth(365);
    artifactColumn.setResizable(false);

    TableColumn versionVersion = new TableViewerColumn(artifactViewer, SWT.NONE).getColumn();
    versionVersion.setText("Version");
    versionVersion.setWidth(205);

    TableColumn profilesColumn = new TableViewerColumn(artifactViewer, SWT.NONE).getColumn();
    profilesColumn.setText("Profiles");
    profilesColumn.setAlignment(SWT.RIGHT);
    profilesColumn.setWidth(56);

    artifactContentProvider = new ArtifactContentProvider();
    artifactViewer.setContentProvider(artifactContentProvider);
    artifactViewer.setLabelProvider(new TableLabelProvider(getDisplay()));
    artifactViewer.addSelectionChangedListener(new SelectionChangedListener()
    {
      @Override
      protected void doSelectionChanged(SelectionChangedEvent event)
      {
        updateArtifactButtons();
      }

      @Override
      protected void triggerOtherSelections(SelectionChangedEvent event)
      {
        if (!changingSelection.contains(profileViewer))
        {
          Set<Profile> profiles = new HashSet<Profile>();
          for (Artifact artifact : getSelectedArtifacts())
          {
            profiles.addAll(artifact.getProfiles());
          }

          profileViewer.setSelection(new StructuredSelection(profiles.toArray()));
        }
      }
    });

    Composite artifactButtonBar = new Composite(artifactComposite, SWT.NONE);
    artifactButtonBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    GridLayout artifactButtonBarLayout = new GridLayout(5, false);
    artifactButtonBarLayout.marginWidth = 0;
    artifactButtonBarLayout.marginHeight = 0;
    artifactButtonBar.setLayout(artifactButtonBarLayout);

    final Combo filterCombo = new Combo(artifactButtonBar, SWT.READ_ONLY);
    filterCombo.add(ArtifactContentProvider.SHOW_ALL);
    filterCombo.add(ArtifactContentProvider.SHOW_UNUSED);
    filterCombo.add(ArtifactContentProvider.SHOW_DAMAGED);
    filterCombo.select(0);
    filterCombo.pack();
    filterCombo.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        String selection = filterCombo.getText();
        artifactContentProvider.setFilter(selection);
        // artifactViewer.setItemCount(artifactContentProvider.getElements(currentBundlePool).length);
        // artifactViewer.refresh();
        updateArtifactButtons();
      }
    });

    selectAllArtifactsButton = new Button(artifactButtonBar, SWT.NONE);
    selectAllArtifactsButton.setText("Select All");
    selectAllArtifactsButton.setEnabled(false);
    selectAllArtifactsButton.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        artifactViewer.setSelection(new StructuredSelection(currentBundlePool.getArtifacts()));
      }
    });

    new Label(artifactButtonBar, SWT.NONE).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    repairArtifactsButton = new Button(artifactButtonBar, SWT.NONE);
    repairArtifactsButton.setText("Repair");
    repairArtifactsButton.setEnabled(false);
    repairArtifactsButton.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        Artifact[] artifacts = getSelectedArtifacts();
        repairArtifacts(artifacts);
      }
    });

    deleteArtifactsButton = new Button(artifactButtonBar, SWT.NONE);
    deleteArtifactsButton.setText("Delete");
    deleteArtifactsButton.setEnabled(false);
    new Label(artifactButtonBar, SWT.NONE);
    new Label(artifactButtonBar, SWT.NONE);
    new Label(artifactButtonBar, SWT.NONE);
    new Label(artifactButtonBar, SWT.NONE);
    new Label(artifactButtonBar, SWT.NONE);
    deleteArtifactsButton.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        Artifact[] artifacts = getSelectedArtifacts();
        deleteArtifacts(artifacts);
      }
    });

    new Label(artifactComposite, SWT.NONE);
  }

  private void createProfileViewer(Composite parent)
  {
    Composite profileComposite = new Composite(parent, SWT.NONE);
    GridLayout profileLayout = new GridLayout(1, false);
    profileLayout.marginWidth = 0;
    profileLayout.marginHeight = 0;
    profileComposite.setLayout(profileLayout);

    profileViewer = new TableViewer(profileComposite, TABLE_STYLE | SWT.MULTI);
    Table profileTable = profileViewer.getTable();
    profileTable.setHeaderVisible(true);
    profileTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    new SelectAllAdapter(profileViewer);

    TableColumn profileColumn = new TableViewerColumn(profileViewer, SWT.NONE).getColumn();
    profileColumn.setText("Profile");
    profileColumn.setWidth(447);
    profileColumn.setResizable(false);

    TableColumn artifactsColumn = new TableViewerColumn(profileViewer, SWT.NONE).getColumn();
    artifactsColumn.setText("Artifacts");
    artifactsColumn.setAlignment(SWT.RIGHT);
    artifactsColumn.setWidth(62);

    TableColumn damagedArtifactsColumn = new TableViewerColumn(profileViewer, SWT.NONE).getColumn();
    damagedArtifactsColumn.setText("Damaged Artifacts");
    damagedArtifactsColumn.setAlignment(SWT.RIGHT);
    damagedArtifactsColumn.setWidth(125);

    profileContentProvider = new ProfileContentProvider();
    profileViewer.setContentProvider(profileContentProvider);
    profileViewer.setLabelProvider(new TableLabelProvider(getDisplay()));
    profileViewer.addSelectionChangedListener(new SelectionChangedListener()
    {
      @Override
      protected void doSelectionChanged(SelectionChangedEvent event)
      {
        // BundlePool bundlePool = getCurrentBundlePool();
        // updateProfileButtons(bundlePool);
      }

      @Override
      protected void triggerOtherSelections(SelectionChangedEvent event)
      {
        if (!changingSelection.contains(artifactViewer))
        {
          Set<Artifact> artifacts = new HashSet<Artifact>();
          for (Profile profile : getSelectedProfiles())
          {
            artifacts.addAll(profile.getArtifacts());
          }

          artifactViewer.setSelection(new StructuredSelection(new ArrayList<Artifact>(artifacts)));
        }
      }
    });

    // Composite profilesButtonBar = new Composite(profilesComposite, SWT.NONE);
    // GridLayout profilesButtonBarLayout = new GridLayout(1, false);
    // profilesButtonBarLayout.marginWidth = 0;
    // profilesButtonBarLayout.marginHeight = 0;
    // profilesButtonBar.setLayout(profilesButtonBarLayout);
    // profilesButtonBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
    //
    // deleteProfileButton = new Button(profilesButtonBar, SWT.NONE);
    // deleteProfileButton.setText("Delete...");
    // deleteProfileButton.setEnabled(false);
    // deleteProfileButton.addSelectionListener(new SelectionAdapter()
    // {
    // @Override
    // public void widgetSelected(SelectionEvent e)
    // {
    // Profile[] profiles = getSelectedProfiles(profilesViewer);
    // deleteProfiles(profiles);
    // }
    // });
  }

  @Override
  protected void checkSubclass()
  {
    // Disable the check that prevents subclassing of SWT components
  }

  private void initAgent()
  {
    analyzer = new BundlePoolAnalyzer(new Handler()
    {
      public void analyzerChanged(final BundlePoolAnalyzer analyzer)
      {
        if (analyzer == BundlePoolComposite.this.analyzer)
        {
          asyncExec(new Runnable()
          {
            public void run()
            {
              bundlePoolViewer.refresh();
              artifactViewer.refresh();
              profileViewer.refresh();

              getDisplay().asyncExec(new Runnable()
              {
                public void run()
                {
                  Object[] elements = bundlePoolContentProvider.getElements(analyzer);
                  if (elements.length != 0)
                  {
                    bundlePoolViewer.setSelection(new StructuredSelection(elements[0]));
                  }
                }
              });
            }
          });
        }
      }

      public void bundlePoolChanged(final BundlePool bundlePool, final boolean artifacts, final boolean profiles)
      {
        asyncExec(new Runnable()
        {
          public void run()
          {
            bundlePoolViewer.update(bundlePool, null);

            if (bundlePool == currentBundlePool)
            {
              if (artifacts)
              {
                artifactViewer.refresh();
                artifactViewer.setItemCount(artifactContentProvider.getElements(bundlePool).length);
                updateArtifactButtons();
              }

              if (profiles)
              {
                profileViewer.refresh();
                profileViewer.setItemCount(profileContentProvider.getElements(bundlePool).length);
              }
            }
          }
        });
      }

      public void profileChanged(final Profile profile)
      {
        if (profile.getBundlePool() == currentBundlePool)
        {
          asyncExec(new Runnable()
          {
            public void run()
            {
              profileViewer.update(profile, null);
            }
          });
        }
      }

      public void artifactChanged(final Artifact artifact)
      {
        if (artifact.getBundlePool() == currentBundlePool)
        {
          asyncExec(new Runnable()
          {
            public void run()
            {
              artifactViewer.update(artifact, null);
            }
          });
        }
      }
    });
  }

  private void asyncExec(Runnable runnable)
  {
    if (!isDisposed())
    {
      Display display = getDisplay();
      UIUtil.asyncExec(display, runnable);
    }
  }

  private Artifact[] getSelectedArtifacts()
  {
    IStructuredSelection selection = (IStructuredSelection)artifactViewer.getSelection();

    @SuppressWarnings("unchecked")
    List<Artifact> artifacts = (List<Artifact>)(List<?>)selection.toList();
    return artifacts.toArray(new Artifact[artifacts.size()]);
  }

  private Profile[] getSelectedProfiles()
  {
    IStructuredSelection selection = (IStructuredSelection)profileViewer.getSelection();

    @SuppressWarnings("unchecked")
    List<Profile> profiles = (List<Profile>)(List<?>)selection.toList();
    return profiles.toArray(new Profile[profiles.size()]);
  }

  private boolean updateButton(Button button, String text, int count, String suffix)
  {
    button.setEnabled(count != 0);
    if (count != 0)
    {
      text += " " + count;
    }

    text += suffix;

    if (!button.getText().equals(text))
    {
      button.setText(text);
      return true;
    }

    return false;
  }

  private void updateArtifactButtons()
  {
    boolean changed = false;

    if (currentBundlePool != null)
    {
      Object[] elements = artifactContentProvider.getElements(currentBundlePool);
      changed |= updateButton(selectAllArtifactsButton, "Select All", elements.length, "");
    }

    Artifact[] artifacts = getSelectedArtifacts();
    int count = artifacts.length;

    changed |= updateButton(deleteArtifactsButton, "Delete", count, " Selected");
    changed |= updateButton(repairArtifactsButton, "Repair", count, " Selected");

    if (changed)
    {
      Composite parent = repairArtifactsButton.getParent();
      parent.pack();
      parent.getParent().layout();
    }
  }

  // private void updateProfileButtons(BundlePool bundlePool)
  // {
  // if (bundlePool == getCurrentBundlePool())
  // {
  // updateButton(profilesViewer, deleteProfileButton, "Delete", true);
  // }
  // }
  //
  // private void moveBundlePool(BundlePool bundlePool)
  // {
  // MessageDialog.openInformation(getShell(), AbstractSetupDialog.SHELL_TEXT, "Not yet implemented.");
  // }
  //
  // private void deleteBundlePool(final BundlePool bundlePool)
  // {
  // MessageDialog.openInformation(getShell(), AbstractSetupDialog.SHELL_TEXT, "Not yet implemented.");
  // }

  private void deleteArtifacts(final Artifact[] artifacts)
  {
    try
    {
      UIUtil.runInProgressDialog(getShell(), new IRunnableWithProgress()
      {
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
        {
          SubMonitor progress = SubMonitor.convert(monitor, "Deleting artifacts", artifacts.length).detectCancelation();

          for (Artifact artifact : artifacts)
          {
            artifact.delete(progress.newChild());
          }
        }
      });

      artifactViewer.refresh();
    }
    catch (InvocationTargetException ex)
    {
      ErrorDialog.open(ex);
    }
    catch (InterruptedException ex)
    {
      throw new OperationCanceledException();
    }
  }

  private void repairArtifacts(final Artifact[] artifacts)
  {
    try
    {
      final List<Artifact> remaining = new ArrayList<Artifact>();

      UIUtil.runInProgressDialog(getShell(), new IRunnableWithProgress()
      {
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
        {
          SubMonitor progress = SubMonitor.convert(monitor, Artifact.REPAIR_TASK_NAME, artifacts.length)
              .detectCancelation();

          for (Artifact artifact : artifacts)
          {
            if (!artifact.repair(null, progress.newChild()))
            {
              remaining.add(artifact);
            }
          }
        }
      });

      if (!remaining.isEmpty())
      {
        new AdditionalURIPrompterDialog(getShell(), remaining);
      }
    }
    catch (InvocationTargetException ex)
    {
      ErrorDialog.open(ex);
    }
    catch (InterruptedException ex)
    {
      throw new OperationCanceledException();
    }
  }

  // private void deleteProfiles(Profile[] profiles)
  // {
  // MessageDialog.openInformation(getShell(), AbstractSetupDialog.SHELL_TEXT, "Not yet implemented.");
  // }

  /**
   * @author Eike Stepper
   */
  private abstract class SelectionChangedListener implements ISelectionChangedListener
  {
    public final void selectionChanged(SelectionChangedEvent event)
    {
      doSelectionChanged(event);

      if (changingSelection.add(event.getSelectionProvider()))
      {
        try
        {
          triggerOtherSelections(event);
        }
        finally
        {
          changingSelection.remove(event.getSelectionProvider());
        }
      }
    }

    protected abstract void doSelectionChanged(SelectionChangedEvent event);

    protected void triggerOtherSelections(SelectionChangedEvent event)
    {
      // Subclasses may override
    }
  }

  // private void deleteProfiles(Profile[] profiles)
  // {
  // MessageDialog.openInformation(getShell(), AbstractSetupDialog.SHELL_TEXT, "Not yet implemented.");
  // }

  /**
   * @author Eike Stepper
   */
  private static final class SelectAllAdapter extends KeyAdapter
  {
    private final StructuredViewer viewer;

    public SelectAllAdapter(StructuredViewer viewer)
    {
      this.viewer = viewer;
      viewer.getControl().addKeyListener(this);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
      if ((e.stateMask & SWT.CONTROL) != 0 && e.keyCode == 'a')
      {
        IStructuredContentProvider contentProvider = (IStructuredContentProvider)viewer.getContentProvider();
        viewer.setSelection(new StructuredSelection(contentProvider.getElements(viewer.getInput())));
      }
    }
  }

  /**
   * @author Eike Stepper
   */
  public static final class TableLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider
  {
    private final Color gray;

    public TableLabelProvider(Display display)
    {
      gray = display.getSystemColor(SWT.COLOR_DARK_GRAY);
    }

    public String getColumnText(Object element, int columnIndex)
    {
      if (element instanceof BundlePool)
      {
        BundlePool bundlePool = (BundlePool)element;
        switch (columnIndex)
        {
        case 0:
          return bundlePool.getLocation().getAbsolutePath();
        case 1:
          return Integer.toString(bundlePool.getArtifactCount());
        case 2:
          return Integer.toString(bundlePool.getUnusedArtifactsCount());
        case 3:
          int percent = bundlePool.getDamagedArtifactsPercent();
          return Integer.toString(bundlePool.getDamagedArtifactsCount())
              + (percent == 100 ? "" : " (" + percent + "%)");
        case 4:
          return Integer.toString(bundlePool.getProfilesCount());
        }
      }
      else if (element instanceof Artifact)
      {
        Artifact artifact = (Artifact)element;
        switch (columnIndex)
        {
        case 0:
          return artifact.getID();
        case 1:
          return artifact.getVersion();
        case 2:
          return Integer.toString(artifact.getProfiles().size());
        }
      }
      else if (element instanceof Profile)
      {
        Profile profile = (Profile)element;
        switch (columnIndex)
        {
        case 0:
          return profile.getID();
        case 1:
          return Integer.toString(profile.getArtifacts().size());
        case 2:
          return Integer.toString(profile.getDamagedArtifactsCount());
        }
      }

      return String.valueOf(element);
    }

    public Image getColumnImage(Object element, int columnIndex)
    {
      if (columnIndex == 0)
      {
        if (element instanceof BundlePool)
        {
          BundlePool bundlePool = (BundlePool)element;
          String key = "bundlePool";
          if (bundlePool.getDamagedArtifactsCount() != 0)
          {
            key += "Damaged";
          }

          return getPluginImage(key);
        }

        if (element instanceof Artifact)
        {
          Artifact artifact = (Artifact)element;
          String key = "artifact" + artifact.getType();
          if (artifact.isDamaged())
          {
            key += "Damaged";
          }

          return getPluginImage(key);
        }

        if (element instanceof Profile)
        {
          Profile profile = (Profile)element;
          String key = "profile" + profile.getType();
          if (profile.isDamaged())
          {
            key += "Damaged";
          }

          return getPluginImage(key);
        }

        if (element instanceof URI)
        {
          return getPluginImage("repository");
        }
      }

      return null;
    }

    public Color getForeground(Object element)
    {
      if (element instanceof Artifact)
      {
        Artifact artifact = (Artifact)element;
        if (artifact.isUnused())
        {
          return gray;
        }
      }

      return null;
    }

    public Color getBackground(Object element)
    {
      return null;
    }

    private static Image getPluginImage(String key)
    {
      return ResourceManager.getPluginImage("org.eclipse.emf.cdo.releng.setup", "icons/obj16/" + key + ".gif");
    }
  }

  /**
   * @author Eike Stepper
   */
  private static abstract class TableContentProvider extends ControlAdapter implements IStructuredContentProvider,
  ILazyContentProvider
  {
    public static final String SHOW_ALL = "Show All";

    private static final int FIRST_TIME = -1;

    private final ControlListener columnListener = new ControlAdapter()
    {
      @Override
      public void controlResized(ControlEvent e)
      {
        resizeColumns(true);
      }
    };

    private TableViewer tableViewer;

    private Object input;

    private String filter = SHOW_ALL;

    private int lastWidth = FIRST_TIME;

    public void setInput(TableViewer viewer, Object input)
    {
      if (tableViewer == null)
      {
        tableViewer = viewer;
        tableViewer.getTable().addControlListener(this);
      }

      Object[] elements = getElements(input);
      tableViewer.setInput(input);
      tableViewer.setItemCount(elements.length);
      tableViewer.refresh();

      ScrollBar verticalBar = tableViewer.getTable().getVerticalBar();
      if (verticalBar != null)
      {
        verticalBar.setSelection(verticalBar.getMinimum());
      }

      resizeColumns();
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
      input = newInput;
    }

    public void dispose()
    {
      input = null;
      tableViewer = null;
    }

    public void updateElement(int index)
    {
      Object[] elements = getElements(input);
      Object element = index < elements.length ? elements[index] : null;
      tableViewer.replace(element, index);
    }

    public boolean isFiltered(String filter)
    {
      return ObjectUtil.equals(filter, this.filter);
    }

    public void setFilter(String filter)
    {
      this.filter = filter;
      UIUtil.asyncExec(tableViewer.getTable().getDisplay(), new Runnable()
      {
        public void run()
        {
          tableViewer.setSelection(StructuredSelection.EMPTY);
          tableViewer.setItemCount(getElements(input).length);
          tableViewer.refresh();
        }
      });
    }

    @Override
    public void controlResized(ControlEvent e)
    {
      resizeColumns(false);
    }

    public void resizeColumns()
    {
      Table table = tableViewer.getTable();
      if (!table.isDisposed())
      {
        UIUtil.asyncExec(table.getDisplay(), new Runnable()
        {
          public void run()
          {
            resizeColumns(true);
          }
        });
      }
    }

    private void resizeColumns(boolean force)
    {
      Table table = tableViewer.getTable();
      int tableWidth = table.getSize().x;
      if (force || tableWidth != lastWidth)
      {
        boolean firstTime = lastWidth == FIRST_TIME;
        lastWidth = tableWidth;

        ScrollBar bar = table.getVerticalBar();
        if (bar != null && bar.isVisible())
        {
          tableWidth -= bar.getSize().x;
        }

        final TableColumn[] columns = table.getColumns();
        for (int i = 1; i < columns.length; i++)
        {
          TableColumn column = columns[i];
          tableWidth -= column.getWidth();

          if (firstTime)
          {
            column.addControlListener(columnListener);
          }
        }

        columns[0].setWidth(tableWidth);
      }
    }
  }

  /**
   * @author Eike Stepper
   */
  private static final class BundlePoolContentProvider extends TableContentProvider
  {
    public Object[] getElements(Object input)
    {
      Map<File, BundlePool> map = ((BundlePoolAnalyzer)input).getBundlePools();
      BundlePool[] bundlePools = map.values().toArray(new BundlePool[map.size()]);
      Arrays.sort(bundlePools);
      return bundlePools;
    }
  }

  /**
   * @author Eike Stepper
   */
  private static final class ArtifactContentProvider extends TableContentProvider
  {
    public static final String SHOW_UNUSED = "Unused";

    public static final String SHOW_DAMAGED = "Damaged";

    public Object[] getElements(Object input)
    {
      if (isFiltered(SHOW_UNUSED))
      {
        return ((BundlePool)input).getUnusedArtifacts();
      }
      else if (isFiltered(SHOW_DAMAGED))
      {
        return ((BundlePool)input).getDamagedArtifacts();
      }

      return ((BundlePool)input).getArtifacts();
    }
  }

  /**
   * @author Eike Stepper
   */
  private static final class ProfileContentProvider extends TableContentProvider
  {
    public Object[] getElements(Object input)
    {
      return ((BundlePool)input).getProfiles();
    }
  }
}