package cn.lucifer.sdop;

public class AI {

	public cn.lucifer.sdop.domain.Boss getTopLevel(
			cn.lucifer.sdop.domain.Boss[] bosses) {
		cn.lucifer.sdop.domain.Boss target = null;
		for (cn.lucifer.sdop.domain.Boss _currentBoss : bosses) {
			if (target != null) {
				// if (_currentBoss.isForRecommend) {
				// if (!target.isForRecommend) {//目标是不推荐
				// target = _currentBoss;
				// continue;
				// }
				// } else {//不推荐
				// continue;
				// }
				if (target.level > _currentBoss.level) {// 判断等级
					continue;
				} else if (target.level < _currentBoss.level) {
					target = _currentBoss;
					continue;
				}
				if (target.currentHp < _currentBoss.currentHp) {// 判断剩余血量
					target = _currentBoss;
				}
			} else {
				target = _currentBoss;
			}
		}
		return target;
	}

	public void startAutoSuperRaidBoss(int delayTime) {

	}
}
