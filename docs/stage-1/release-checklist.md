# Stage 1 release checklist

The assignment specifies the exact tag `stage-1` on a runnable commit. It does not prescribe a commit message, a Conventional Commits prefix or a special commit type. An annotated Git tag is recommended for a useful description; the annotation itself is not stated as a requirement.

- [ ] Review the source and README changes; confirm Stage 1 scope, including the treatment of later-stage code preserved as comments.
- [ ] Run a clean build and both documented replays on the intended release source.
- [ ] Complete and link the matching Stage 1 class diagram. The final submission requires a class diagram for every stage, not only the final one.
- [ ] Commit reviewed implementation and evidence. Suggested message: `Complete Stage 1 game and add verification notes`.
- [ ] Push the branch and merge it through the group's usual review process. Verify the exact resulting commit and clean working tree before tagging it.
- [ ] Create `stage-1` on that verified commit and push that tag. Do not move an existing published stage tag without group agreement.
- [ ] Replace the README's pending Stage 1 status with the actual tag link, and verify it opens for the intended readers.
- [ ] Start Stage 2 from the saved Stage 1 version and retain regression evidence.

Example commands only after the release commit has been reviewed, committed, pushed and checked out locally:

```powershell
git status
git log -1 --oneline
git tag -a stage-1 -m "Runnable Stage 1 baseline"
git push origin stage-1
```

These commands tag the current HEAD. Check that it is the intended reviewed commit; they do not commit uncommitted files. Branch names and commit messages do not replace the required tag.

For final submission, also finish stage comparisons, all diagrams, the report and individual reflections, video timestamps, full individual AI-history links and declarations. Only link artifacts that actually exist.
