//@flow
import type { Links } from "../../types/hal";

export type Permission = {
  name: string,
  type: string,
  groupPermission: boolean,
  _links?: Links
};

export type PermissionCollection = Permission[];
