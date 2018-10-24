/*
 *Copyright(c)2015, InMindComputingAG.Allrightsreserved.
 *IMCPROPRIETARY/CONFIDENTIAL.Useissubjecttolicenseterms.
 */
package test.customizing.issmeillertest01;

import com.er.test.util.jena.RoleTestVocabulary;
import com.hp.hpl.jena.rdf.model.Resource;

/**
*
*
*/
public class ReadRoleMatrix {

  private static RoleTestVocabulary v = new RoleTestVocabulary();

  // Admin
  private static final Resource ADM = v.U_ADMIN;

  // Sales Area Reps and Managers
  private static final Resource GBM = v.U_GB_MITTE;
  private static final Resource RLM = v.U_RL_MITTE;

  private static final Resource GBN = v.U_GB_NORD;
  private static final Resource RLN = v.U_RL_NORD;

  private static final Resource GBS = v.U_GB_SUED;
  private static final Resource RLS = v.U_RL_SUED;

  private static final Resource GBW = v.U_GB_WEST;
  private static final Resource RLW = v.U_RL_WEST;

  private static final Resource KAM = v.U_KAM;

  // Higher Management / GK

  private static final Resource VL = v.U_VL;
  private static final Resource GK = v.U_GK;
  private static final Resource GF = v.U_GF;

  // Supporting Role

  private static final Resource AL = v.U_AL;


  /**
   * has access
   */
  private final static boolean x = true;
  /**
   * has no access
   */
  private final static boolean _ = false;

  /**
   * Reusable roles
   */
  public static final Resource[] roleArray = new Resource[]{
        ADM, GBM, RLM, GBN, RLN, GBS, RLS, GBW, RLW, KAM,  VL,  GK,  GF,
  }; //   0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,

  /**
   * Access matrix of re-usable roles (concrete user will use combinations of them for reading and writing)
   */
  public static boolean accessMatrix[][] = new boolean[][]{
      // | object accessor (read)
      // V
      // ---> object owner (write)
      //
      //            0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,
      //          ADM, GBM, RLM, GBN, RLN, GBS, RLS, GBW, RLW, KAM,  VL,  GK,  GF,
      /*  0 ADM */{ x,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,},
      /*  1 GBM */{ x,  x ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,},
      /*  2 RLM */{ x,  x ,  x ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,},
      /*  3 GBN */{ x,  _ ,  _ ,  x ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,},
      /*  4 RLN */{ x,  _ ,  _ ,  x ,  x ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,},
      /*  5 GBS */{ x,  _ ,  _ ,  _ ,  _ ,  x ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,},
      /*  6 RLS */{ x,  _ ,  _ ,  _ ,  _ ,  x ,  x ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,},
      /*  7 GBW */{ x,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  x ,  _ ,  _ ,  _ ,  _ ,  _ ,},
      /*  8 RLW */{ x,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  x ,  x ,  _ ,  _ ,  _ ,  _ ,},
      /*  9 KAM */{ x,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  _ ,  x ,  _ ,  _ ,  _ ,},
      /* 10  VL */{ x , x ,  x ,  x ,  x ,  x ,  x ,  x ,  x ,  _ ,  x ,  _ ,  _ ,},
      /* 11  GK */{ x,  x ,  x ,  x ,  x ,  x ,  x ,  x ,  x ,  _ ,  _ ,  x ,  _ ,},
      /* 12  GF */{ x,  x ,  x ,  x ,  x ,  x ,  x ,  x ,  x ,  x ,  x ,  x ,  x ,},
   };
}
