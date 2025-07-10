{
  description = "xDECAF - An extensible data flow diagram constraint analysis framework";

  inputs = {
    nixpkgs.url = "github:nixOS/nixpkgs/nixos-unstable";
  };

  outputs = {nixpkgs, ...}: let
    supportedSystems = ["x86_64-linux" "aarch64-linux" "x86_64-darwin" "aarch64-darwin"];

    forAllSystems = nixpkgs.lib.genAttrs supportedSystems;
  in {
    devShells = forAllSystems (system: let
      pkgs = import nixpkgs {inherit system;};
    in {
      default = nixpkgs.legacyPackages.${system}.mkShellNoCC {
        shellHook = ''
          rm -rf .husky/_
          ${pkgs.husky}/bin/husky install .husky
        '';
        packages = with pkgs; [
          husky
          maven
          python3
          openjdk17
        ];
      };
    });
  };
}
