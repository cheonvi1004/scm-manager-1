// @flow
import type { File, FileChangeType, Hunk } from "./DiffTypes";
import {
  getPath,
  createHunkIdentifier,
  createHunkIdentifierFromContext
} from "./diffs";

describe("tests for diff util functions", () => {
  const file = (
    type: FileChangeType,
    oldPath: string,
    newPath: string
  ): File => {
    return {
      hunks: [],
      type: type,
      oldPath,
      newPath,
      newEndingNewLine: true,
      oldEndingNewLine: true
    };
  };

  const add = (path: string) => {
    return file("add", "/dev/null", path);
  };

  const rm = (path: string) => {
    return file("delete", path, "/dev/null");
  };

  const modify = (path: string) => {
    return file("modify", path, path);
  };

  const createHunk = (content: string): Hunk => {
    return {
      content,
      changes: []
    };
  };

  describe("getPath tests", () => {
    it("should pick the new path, for type add", () => {
      const file = add("/etc/passwd");
      const path = getPath(file);
      expect(path).toBe("/etc/passwd");
    });

    it("should pick the old path, for type delete", () => {
      const file = rm("/etc/passwd");
      const path = getPath(file);
      expect(path).toBe("/etc/passwd");
    });
  });

  describe("createHunkIdentifier tests", () => {
    it("should create identifier", () => {
      const file = modify("/etc/passwd");
      const hunk = createHunk("@@ -1,18 +1,15 @@");
      const identifier = createHunkIdentifier(file, hunk);
      expect(identifier).toBe("modify_/etc/passwd_@@ -1,18 +1,15 @@");
    });
  });

  describe("createHunkIdentifierFromContext tests", () => {
    it("should create identifier", () => {
      const identifier = createHunkIdentifierFromContext({
        file: rm("/etc/passwd"),
        hunk: createHunk("@@ -1,42 +1,39 @@")
      });
      expect(identifier).toBe("delete_/etc/passwd_@@ -1,42 +1,39 @@");
    });
  });
});
