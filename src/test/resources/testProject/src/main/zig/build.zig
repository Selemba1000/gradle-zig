const std = @import("std");

pub fn build(b: *std.Build) void {
    const target = b.standardTargetOptions(.{});
    const optimize = b.standardOptimizeOption(.{});

    const lib = b.addLibrary(.{
        .name = "foo",
        .linkage = .dynamic,
        .root_module = b.createModule(.{
            .root_source_file = b.path("foo.zig"),
            .target = target,
            .optimize = optimize,
        })
    });
    if(target.result.cpu.arch == .x86){
        lib.link_z_notext = true;
    }

    const lib_output = b.addInstallArtifact(lib, .{
        .implib_dir = .disabled,
        .dest_dir = .{ .override = .lib },
        .pdb_dir = .disabled
    });
    b.getInstallStep().dependOn(&lib_output.step);
}