import Foundation
import SharedLogic

@MainActor
final class WorkspaceViewModel: ObservableObject {

    @Published private(set)
    var state: TasksUiState

    private let controller:
        IosTasksController

    private var isObserving =
        false

    init() {
        let controller =
            IosTasksController()

        self.controller =
            controller

        self.state =
            controller.currentState()
    }

    func start() {
        guard !isObserving else {
            return
        }

        isObserving = true

        controller.startObserving {
            [weak self] newState in

            self?.state =
                newState
        }
    }

    func stop() {
        controller.stopObserving()
        isObserving = false
    }

    func refresh() {
        controller.refresh()
    }

    func selectFilter(
        _ filter: TaskFilter
    ) {
        controller.selectFilter(
            filter: filter
        )
    }

    func updateStatus(
        taskId: String,
        status: TaskStatus
    ) {
        controller.updateStatus(
            taskId: taskId,
            status: status
        )
    }

    func retrySync() {
        controller.retrySync()
    }

    func useServer(
        taskId: String
    ) {
        controller.useServerVersion(
            taskId: taskId
        )
    }

    func keepMine(
        taskId: String
    ) {
        controller.keepLocalChange(
            taskId: taskId
        )
    }
}
