/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.emf.cdo.releng.doc.article;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Toc</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.emf.cdo.releng.doc.article.Toc#getLevels <em>Levels</em>}</li>
 * </ul>
 * </p>
 * 
 * @see org.eclipse.emf.cdo.releng.doc.article.ArticlePackage#getToc()
 * @model
 * @generated
 */
public interface Toc extends BodyElement
{
  /**
   * Returns the value of the '<em><b>Levels</b></em>' attribute. The default value is <code>"-1"</code>. <!--
   * begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Levels</em>' attribute isn't clear, there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * 
   * @return the value of the '<em>Levels</em>' attribute.
   * @see org.eclipse.emf.cdo.releng.doc.article.ArticlePackage#getToc_Levels()
   * @model default="-1" changeable="false" derived="true"
   * @generated
   */
  int getLevels();

} // Toc