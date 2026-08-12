import SwiftUI
import SharedLogic

struct ContentView: View {

    @StateObject private var viewModel =
        WorkspaceViewModel()

    var body: some View {
        ZStack {
            Color(
                red: 0.969,
                green: 0.973,
                blue: 0.980
            )
            .ignoresSafeArea()

            VStack(spacing: 0) {
                header

                ScrollView(
                    .vertical,
                    showsIndicators: true
                ) {
                    LazyVStack(
                        alignment: .leading,
                        spacing: 16
                    ) {
                        workspaceCard
                        filters
                        issueHeader

                        if viewModel
                            .state
                            .visibleTasks
                            .isEmpty {
                            emptyState
                        } else {
                            ForEach(
                                viewModel
                                    .state
                                    .visibleTasks,
                                id: \.id
                            ) { task in
                                taskCard(task)
                            }
                        }

                        Spacer()
                            .frame(height: 28)
                    }
                    .padding(20)
                    .frame(
                        maxWidth: .infinity,
                        alignment: .leading
                    )
                }
                .frame(
                    maxWidth: .infinity,
                    maxHeight: .infinity
                )
                .contentShape(Rectangle())
                .scrollBounceBehavior(.always)
                .refreshable {
                    viewModel.refresh()
                }
            }
            .frame(
                maxWidth: .infinity,
                maxHeight: .infinity
            )
        }
        .onAppear {
            viewModel.start()
        }
        .onDisappear {
            viewModel.stop()
        }
    }

    private var header: some View {
        HStack {
            VStack(
                alignment: .leading,
                spacing: 2
            ) {
                Text("SyncBoard")
                    .font(.title2)
                    .fontWeight(.bold)

                Text("Workspace")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            Spacer()

            Text("HK")
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundStyle(.indigo)
                .frame(
                    width: 42,
                    height: 42
                )
                .background(
                    Color.indigo.opacity(0.08)
                )
                .clipShape(Circle())
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 14)
        .background(Color.white)
    }

    private var workspaceCard: some View {
        let tasks =
            viewModel.state.tasks

        let backlog =
            tasks.filter {
                $0.status.name ==
                    "BACKLOG"
            }.count

        let progress =
            tasks.filter {
                $0.status.name ==
                    "IN_PROGRESS"
            }.count

        let done =
            tasks.filter {
                $0.status.name ==
                    "DONE"
            }.count

        return VStack(
            alignment: .leading,
            spacing: 20
        ) {
            HStack {
                VStack(
                    alignment: .leading,
                    spacing: 4
                ) {
                    Text(
                        viewModel
                            .state
                            .projectName
                    )
                    .font(.headline)

                    Text("Mobile platform")
                        .font(.subheadline)
                        .foregroundStyle(
                            Color.white.opacity(
                                0.62
                            )
                        )
                }

                Spacer()

                syncBadge
            }

            HStack(spacing: 34) {
                metric(
                    value: backlog,
                    label: "Open"
                )

                metric(
                    value: progress,
                    label: "In progress"
                )

                metric(
                    value: done,
                    label: "Completed"
                )
            }
        }
        .foregroundStyle(.white)
        .padding(20)
        .frame(maxWidth: .infinity)
        .background(
            Color(
                red: 0.098,
                green: 0.137,
                blue: 0.216
            )
        )
        .clipShape(
            RoundedRectangle(
                cornerRadius: 22
            )
        )
    }

    private var syncBadge: some View {
        let conflicts =
            viewModel.state.conflictCount

        let pending =
            viewModel.state.pendingChanges

        let label: String =
            conflicts > 0
            ? "\(conflicts) conflict"
            : pending > 0
            ? "\(pending) pending"
            : viewModel.state.isRefreshing
            ? "Syncing"
            : "Synced"

        let color: Color =
            conflicts > 0
            ? .red
            : pending > 0
            ? .orange
            : .green

        return HStack(spacing: 7) {
            Circle()
                .fill(color)
                .frame(
                    width: 7,
                    height: 7
                )

            Text(label)
                .font(.caption)
                .fontWeight(.medium)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(
            Color.white.opacity(0.08)
        )
        .clipShape(Capsule())
    }

    private var filters: some View {
        HStack(spacing: 8) {
            filterButton(
                title: "All",
                filter: TaskFilter.all
            )

            filterButton(
                title: "Backlog",
                filter: TaskFilter.backlog
            )

            filterButton(
                title: "In progress",
                filter: TaskFilter.inProgress
            )

            filterButton(
                title: "Done",
                filter: TaskFilter.done
            )
        }
    }

    private var issueHeader: some View {
        HStack {
            Text("Engineering")
                .font(.headline)

            Spacer()

            Text(
                "\(viewModel.state.visibleTasks.count) issues"
            )
            .font(.caption)
            .foregroundStyle(.secondary)
        }
        .padding(.top, 2)
    }

    private var emptyState: some View {
        VStack(spacing: 7) {
            Text("No issues here")
                .font(.headline)

            Text(
                "Try another status filter."
            )
            .font(.subheadline)
            .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 54)
    }

    private func taskCard(
        _ task: SharedLogic.Task
    ) -> some View {
        VStack(
            alignment: .leading,
            spacing: 14
        ) {
            HStack {
                Text(task.id.uppercased())
                    .font(.caption)
                    .fontWeight(.medium)
                    .foregroundStyle(
                        Color(
                            red: 0.40,
                            green: 0.47,
                            blue: 0.58
                        )
                    )

                Spacer()

                priorityPill(
                    task.priority.name
                )
            }

            Text(task.title)
                .font(.headline)
                .foregroundStyle(
                    Color(
                        red: 0.067,
                        green: 0.094,
                        blue: 0.153
                    )
                )

            if task.syncStatus.name ==
                "CONFLICT" {
                conflictPanel(task)
            }

            HStack {
                statusMenu(task)

                Spacer()

                Text("HK")
                    .font(.caption2)
                    .fontWeight(.semibold)
                    .frame(
                        width: 30,
                        height: 30
                    )
                    .background(
                        Color(
                            red: 0.949,
                            green: 0.957,
                            blue: 0.969
                        )
                    )
                    .clipShape(Circle())
            }
        }
        .padding(16)
        .background(Color.white)
        .clipShape(
            RoundedRectangle(
                cornerRadius: 16
            )
        )
        .overlay {
            RoundedRectangle(
                cornerRadius: 16
            )
            .stroke(
                Color.black.opacity(0.09)
            )
        }
    }

    private func conflictPanel(
        _ task: SharedLogic.Task
    ) -> some View {
        VStack(
            alignment: .leading,
            spacing: 8
        ) {
            Text("Sync conflict")
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundStyle(.red)

            Text(
                "This issue changed on another device."
            )
            .font(.caption)
            .foregroundStyle(.red)

            HStack(spacing: 18) {
                Button("Use server") {
                    viewModel.useServer(
                        taskId: task.id
                    )
                }

                Button("Keep mine") {
                    viewModel.keepMine(
                        taskId: task.id
                    )
                }
            }
            .font(.caption)
            .fontWeight(.medium)
            .padding(.top, 2)
        }
        .padding(12)
        .frame(
            maxWidth: .infinity,
            alignment: .leading
        )
        .background(
            Color.red.opacity(0.06)
        )
        .clipShape(
            RoundedRectangle(
                cornerRadius: 10
            )
        )
    }

    private func statusMenu(
        _ task: SharedLogic.Task
    ) -> some View {
        Menu {
            Button("Backlog") {
                viewModel.updateStatus(
                    taskId: task.id,
                    status:
                        TaskStatus.backlog
                )
            }

            Button("In progress") {
                viewModel.updateStatus(
                    taskId: task.id,
                    status:
                        TaskStatus.inProgress
                )
            }

            Button("Done") {
                viewModel.updateStatus(
                    taskId: task.id,
                    status:
                        TaskStatus.done
                )
            }
        } label: {
            HStack(spacing: 7) {
                Circle()
                    .fill(
                        statusColor(
                            task.status.name
                        )
                    )
                    .frame(
                        width: 7,
                        height: 7
                    )

                Text(
                    statusLabel(
                        task.status.name
                    )
                )
                .font(.caption)

                Image(
                    systemName:
                        "chevron.down"
                )
                .font(.system(size: 8))
            }
            .foregroundStyle(.secondary)
            .padding(.horizontal, 10)
            .padding(.vertical, 7)
            .background(
                Color(
                    red: 0.949,
                    green: 0.957,
                    blue: 0.969
                )
            )
            .clipShape(Capsule())
        }
    }

    private func filterButton(
        title: String,
        filter: TaskFilter
    ) -> some View {
        let selected =
            viewModel.state.filter.name ==
            filter.name

        return Button {
            viewModel.selectFilter(filter)
        } label: {
            Text(title)
                .font(.caption)
                .fontWeight(.medium)
                .foregroundStyle(
                    selected
                    ? Color.indigo
                    : Color.secondary
                )
                .padding(
                    .horizontal,
                    12
                )
                .padding(
                    .vertical,
                    8
                )
                .background(
                    selected
                    ? Color.indigo
                        .opacity(0.09)
                    : Color.white
                )
                .clipShape(
                    RoundedRectangle(
                        cornerRadius: 9
                    )
                )
                .overlay {
                    RoundedRectangle(
                        cornerRadius: 9
                    )
                    .stroke(
                        selected
                        ? Color.clear
                        : Color.black
                            .opacity(0.10)
                    )
                }
        }
        .buttonStyle(.plain)
    }

    private func metric(
        value: Int,
        label: String
    ) -> some View {
        VStack(
            alignment: .leading,
            spacing: 3
        ) {
            Text("\(value)")
                .font(.title2)
                .fontWeight(.bold)

            Text(label)
                .font(.caption2)
                .foregroundStyle(
                    Color.white.opacity(
                        0.58
                    )
                )
        }
    }

    private func priorityPill(
        _ priority: String
    ) -> some View {
        let text =
            priority
                .lowercased()
                .capitalized

        return Text(text)
            .font(.caption2)
            .fontWeight(.medium)
            .foregroundStyle(
                priority == "HIGH" ||
                priority == "URGENT"
                ? Color.red
                : priority == "MEDIUM"
                ? Color.orange
                : Color.secondary
            )
            .padding(
                .horizontal,
                9
            )
            .padding(
                .vertical,
                5
            )
            .background(
                priority == "HIGH" ||
                priority == "URGENT"
                ? Color.red.opacity(0.07)
                : priority == "MEDIUM"
                ? Color.orange.opacity(0.08)
                : Color(
                    red: 0.949,
                    green: 0.957,
                    blue: 0.969
                )
            )
            .clipShape(Capsule())
    }

    private func statusLabel(
        _ status: String
    ) -> String {
        switch status {
        case "IN_PROGRESS":
            return "In progress"

        case "DONE":
            return "Done"

        default:
            return "Backlog"
        }
    }

    private func statusColor(
        _ status: String
    ) -> Color {
        switch status {
        case "IN_PROGRESS":
            return Color.indigo

        case "DONE":
            return Color.green

        default:
            return Color(
                red: 0.60,
                green: 0.64,
                blue: 0.70
            )
        }
    }
}
