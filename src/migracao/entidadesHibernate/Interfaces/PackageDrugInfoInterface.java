/*
 * Decompiled with CFR 0_114.
 */
package migracao.entidadesHibernate.Interfaces;

import java.io.Serializable;
import java.util.List;

public interface PackageDrugInfoInterface<T, Id extends Serializable> {
    public void persist(T var1);

    public void update(T var1);

    public T findById(Id var1);

    public void delete(T var1);

    public List<T> findAll();
    
     public List<T> findAllbyPatientID(Id var1);

     public void deleteAll();
}

